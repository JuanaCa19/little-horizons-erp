package integration;

import dao.*;
import dao.impl.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import service.*;
import util.exception.ValidationException;

public class ExtracurricularModuleIntegrationTest {
    private static int checks;
    private static void check(boolean value,String message) { if(!value) throw new AssertionError(message);checks++; }
    private static void rejected(Runnable work) {
        try { work.run(); } catch(ValidationException e) { checks++;return; }
        throw new AssertionError("Expected business validation failure");
    }
    private static Club club(String name,int capacity) {
        Club value=new Club();value.setName(name);value.setCapacity(capacity);value.setActive(true);value.setSupervisorId(1);value.setMeetingSchedule("Martes 3 PM");return value;
    }
    private static ClubMembership membership(int clubId,int studentId,LocalDate date) {
        ClubMembership value=new ClubMembership();value.setClubId(clubId);value.setStudentId(studentId);value.setEntryDate(date);return value;
    }
    private static Event event(String name,boolean competition,LocalDate date) {
        Event value=new Event();value.setName(name);value.setType(EventType.ACADEMIC);value.setDate(date);value.setCompetition(competition);value.setOrganizerId(1);value.setActive(true);return value;
    }
    private static EventParticipant participant(int eventId,int studentId,EventRole role,EventPosition position) {
        EventParticipant value=new EventParticipant();value.setEventId(eventId);value.setStudentId(studentId);value.setRole(role);value.setPosition(position);return value;
    }
    public static void main(String[] args) throws Exception {
        LocalDate today=LocalDate.now();LocalDate entry=today.minusDays(10),withdrawal=today.minusDays(3);
        try(TemporaryDatabase db=new TemporaryDatabase("estudiantes","profesores","clubes","membresias_club","eventos","participantes_evento")) {
            db.execute("INSERT INTO estudiantes(id_estudiante,tipo_documento,numero_documento,primer_nombre,primer_apellido,fecha_nacimiento,genero,fecha_ingreso,estado) VALUES "
                    + "(1,'TI','111','Ana','Perez','2010-01-01','F','2020-01-01','Activo'),(2,'TI','222','Luis','Ruiz','2010-01-01','M','2020-01-01','Activo'),(3,'TI','333','Sara','Diaz','2010-01-01','F','2020-01-01','Retirado')");
            db.execute("INSERT INTO profesores(id_profesor,tipo_documento,numero_documento,primer_nombre,primer_apellido,correo_electronico,fecha_contratacion,estado) VALUES "
                    + "(1,'CC','100','Maria','Gil','maria@example.com','2020-01-01','Activo'),(2,'CC','200','Jose','Diaz','jose@example.com','2020-01-01','Inactivo')");
            ClubService clubs=new ClubService(new ClubDAOImpl(db.connections()),new ClubMembershipDAOImpl(db.connections()),new ExtracurricularContextDAOImpl(),db.connections());
            EventService events=new EventService(new EventDAOImpl(db.connections()),new EventParticipantDAOImpl(db.connections()),new ExtracurricularContextDAOImpl(),db.connections());
            Club club=club(" Robotica ",1);int clubId=clubs.create(club);
            check(club.getName().equals("Robotica") && clubs.getById(clubId).getSupervisorId()==1,"Club normalization and mapping");
            check(clubs.findAll().size()==1 && clubs.availableSeats(clubId)==1,"Catalog and seats");
            rejected(() -> clubs.create(club("ROBOTICA",2)));
            rejected(() -> clubs.create(club("Invalido",0)));
            Club inactiveSupervisor=club("Otro",3);inactiveSupervisor.setSupervisorId(2);rejected(() -> clubs.create(inactiveSupervisor));
            check(inactiveSupervisor.getId()==null,"Failed create does not publish id");
            ClubMembership first=membership(clubId,1,entry);int firstId=clubs.join(first);
            check(first.getStatus()==MembershipStatus.ACTIVE && clubs.getMembership(firstId).getEntryDate().equals(entry),"Membership mapping and default state");
            check(clubs.availableSeats(clubId)==0,"Joining consumes capacity");
            rejected(() -> clubs.join(membership(clubId,1,entry)));
            ClubMembership full=membership(clubId,2,entry);rejected(() -> clubs.join(full));
            check(full.getId()==null && clubs.findMembershipsByClub(clubId).size()==1,"Full club rolls back without extra member");
            club.setCapacity(2);clubs.update(club);clubs.join(membership(clubId,2,entry));
            club.setCapacity(1);rejected(() -> clubs.update(club));
            check(clubs.getById(clubId).getCapacity()==2,"Cannot shrink below active members");club.setCapacity(2);
            rejected(() -> clubs.join(membership(clubId,3,entry)));
            rejected(() -> clubs.join(membership(clubId,1,today.plusDays(1))));
            rejected(() -> clubs.withdraw(firstId,entry.minusDays(1),"Invalid date"));
            rejected(() -> clubs.withdraw(firstId,today.plusDays(1),"Future"));
            clubs.withdraw(firstId,withdrawal,"Cambio de intereses");
            ClubMembership withdrawn=clubs.getMembership(firstId);
            check(withdrawn.getStatus()==MembershipStatus.WITHDRAWN && withdrawn.getWithdrawalDate().equals(withdrawal)
                    && withdrawn.getWithdrawalReason().equals("Cambio de intereses"),"Withdrawal retains dates and reason");
            check(clubs.availableSeats(clubId)==1,"Withdrawal releases capacity");
            rejected(() -> clubs.withdraw(firstId,today,"Again"));
            rejected(() -> clubs.join(membership(clubId,1,withdrawal.minusDays(1))));
            int rejoined=clubs.join(membership(clubId,1,withdrawal.plusDays(1)));
            check(rejoined!=firstId && clubs.findMembershipsByStudent(1).size()==2,"Rejoining creates separate history");
            ClubMembership beforeAdmission=membership(clubId,2,LocalDate.of(2019,1,1));rejected(() -> clubs.join(beforeAdmission));
            club.setActive(false);clubs.update(club);
            rejected(() -> clubs.join(membership(clubId,1,today)));
            clubs.withdraw(rejoined,today,"Fin de actividad");
            check(clubs.getMembership(rejoined).getStatus()==MembershipStatus.WITHDRAWN,"Inactive club can still record withdrawal");
            check(clubs.findMembershipsByClub(clubId).size()==3,"Club history preserves withdrawn memberships");
            rejected(() -> clubs.getMembership(99999));
            Event competition=event("Feria de Ciencias",true,today.minusDays(1));int eventId=events.create(competition);
            check(events.getById(eventId).getType()==EventType.ACADEMIC && events.getById(eventId).isCompetition(),"Event mapping including accented enum");
            Event invalidOrganizer=event("Otro",false,today);invalidOrganizer.setOrganizerId(2);rejected(() -> events.create(invalidOrganizer));
            Event simple=event("Dia cultural",false,today);int simpleId=events.create(simple);
            Event future=event("Concurso futuro",true,today.plusDays(10));int futureId=events.create(future);
            check(events.findByDates(today.minusDays(2),today).size()==2,"Date-filtered calendar");
            rejected(() -> events.findByDates(today,today.minusDays(1)));
            EventParticipant firstPlace=participant(eventId,1,EventRole.COMPETITOR,EventPosition.FIRST);int participationId=events.registerParticipant(firstPlace);
            check(events.getParticipant(participationId).getPosition()==EventPosition.FIRST,"Competition result mapping");
            rejected(() -> events.registerParticipant(participant(eventId,1,EventRole.ATTENDEE,null)));
            rejected(() -> events.registerParticipant(participant(simpleId,1,EventRole.PARTICIPANT,EventPosition.PARTICIPANT)));
            rejected(() -> events.registerParticipant(participant(simpleId,1,EventRole.COMPETITOR,null)));
            rejected(() -> events.registerParticipant(participant(eventId,3,EventRole.COMPETITOR,EventPosition.SECOND)));
            rejected(() -> events.registerParticipant(participant(eventId,2,EventRole.ORGANIZER,EventPosition.SECOND)));
            rejected(() -> events.registerParticipant(participant(eventId,2,EventRole.PARTICIPANT,EventPosition.FIRST)));
            rejected(() -> events.registerParticipant(participant(futureId,1,EventRole.COMPETITOR,EventPosition.FIRST)));
            int preRegistration=events.registerParticipant(participant(futureId,1,EventRole.COMPETITOR,null));
            check(events.getParticipant(preRegistration).getPosition()==null,"Future event allows registration without result");
            events.registerParticipant(participant(simpleId,1,EventRole.ATTENDEE,null));
            check(events.findParticipantsByEvent(simpleId).size()==1,"Noncompetition participation");
            check(events.findParticipantsByStudent(1).size()==3,"Student event history");
            firstPlace.setPosition(EventPosition.SECOND);firstPlace.setNotes("Resultado corregido");events.updateParticipant(firstPlace);
            check(events.getParticipant(participationId).getNotes().equals("Resultado corregido") && events.getParticipant(participationId).getPosition()==EventPosition.SECOND,"Update result without duplicate row");
            firstPlace.setStudentId(2);rejected(() -> events.updateParticipant(firstPlace));firstPlace.setStudentId(1);
            competition.setCompetition(false);rejected(() -> events.update(competition));competition.setCompetition(true);
            competition.setDate(today.plusDays(5));rejected(() -> events.update(competition));competition.setDate(today.minusDays(1));
            competition.setActive(false);events.update(competition);
            rejected(() -> events.registerParticipant(participant(eventId,2,EventRole.COMPETITOR,null)));
            check(events.findParticipantsByEvent(eventId).size()==1,"Deactivation preserves results");
            firstPlace.setPosition(EventPosition.THIRD);events.updateParticipant(firstPlace);
            check(events.getParticipant(participationId).getPosition()==EventPosition.THIRD,"Historical corrections allowed after deactivation");
            simple.setLocation("Auditorio");simple.setDescription("Evento cultural");events.update(simple);
            check(events.getById(simpleId).getLocation().equals("Auditorio"),"Update event details");
            rejected(() -> events.getParticipant(99999));
        }
        System.out.println("Passed "+checks+" extracurricular JDBC checks using temporary tables.");
    }
}
