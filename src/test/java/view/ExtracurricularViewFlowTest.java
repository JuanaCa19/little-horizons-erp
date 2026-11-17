package view;

import controller.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.ValidationException;

/** Run each console scenario in a fresh JVM. */
public class ExtracurricularViewFlowTest {
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        String scenario=args[0];int[] writes={0};
        Student student=new Student();student.setId(1);student.setFirstName("Ana");student.setFirstLastName("Perez");
        Club club=new Club();club.setId(1);club.setName("Robotica");club.setCapacity(2);club.setActive(true);
        ClubMembership membership=new ClubMembership();membership.setId(2);membership.setStudentId(1);membership.setClubId(1);membership.setEntryDate(LocalDate.of(2026,1,1));membership.setStatus(MembershipStatus.ACTIVE);
        Event event=new Event();event.setId(1);event.setName("Feria");event.setType(EventType.SCIENTIFIC);event.setDate(scenario.equals("future-registration") ? LocalDate.now().plusDays(10) : LocalDate.now().minusDays(1));
        event.setCompetition(!scenario.equals("noncompetition"));event.setActive(true);
        EventParticipant original=new EventParticipant();original.setId(3);original.setEventId(1);original.setStudentId(1);original.setRole(EventRole.COMPETITOR);original.setPosition(EventPosition.FIRST);
        ClubController clubs=new ClubController() {
            @Override public Club getById(Integer id) { return club; }
            @Override public int availableSeats(Integer id) { return scenario.equals("full-club") ? 0 : 1; }
            @Override public int create(Club value) { check(value.getName().equals("Robotica") && value.getCapacity()==2 && value.isActive(),"Club form");writes[0]++;return 10; }
            @Override public int join(ClubMembership value) {
                if(scenario.equals("full-club")) throw new ValidationException("Sin cupos");
                check(value.getClubId()==1 && value.getStudentId()==1 && value.getStatus()==MembershipStatus.ACTIVE,"Membership form");writes[0]++;return 20;
            }
            @Override public ClubMembership getMembership(Integer id) { return membership; }
            @Override public void withdraw(Integer id,LocalDate date,String reason) { check(id==2 && reason.equals("Cambio de intereses"),"Withdrawal form");writes[0]++; }
            @Override public List<ClubMembership> findMembershipsByStudent(Integer id) { return List.of(membership); }
        };
        EventController events=new EventController() {
            @Override public Event getById(Integer id) { return event; }
            @Override public List<Event> findByDates(LocalDate from,LocalDate to) { return List.of(event); }
            @Override public int create(Event value) { check(value.getName().equals("Feria") && value.isCompetition() && value.isActive(),"Event form");writes[0]++;return 30; }
            @Override public int registerParticipant(EventParticipant value) {
                check(value.getStudentId()==1 && value.getEventId()==1,"Participation identity");
                if(scenario.equals("noncompetition")) check(value.getRole()==EventRole.ATTENDEE && value.getPosition()==null,"Noncompetition must skip result");
                else if(scenario.equals("future-registration")) check(value.getPosition()==null,"Future event must skip result");
                else check(value.getRole()==EventRole.COMPETITOR && value.getPosition()==EventPosition.FIRST,"Competition form");
                writes[0]++;return 40;
            }
            @Override public EventParticipant getParticipant(Integer id) { return original; }
            @Override public void updateParticipant(EventParticipant value) { check(value!=original && value.getId()==3 && value.getPosition()==EventPosition.SECOND,"Result correction");writes[0]++; }
            @Override public List<EventParticipant> findParticipantsByStudent(Integer id) { return List.of(original); }
        };
        StudentController students=new StudentController() { @Override public Student getById(Integer id) { return student; } };
        TeacherController teachers=new TeacherController();
        ClubView clubView=new ClubView(clubs,students,teachers);EventView eventView=new EventView(events,students,teachers);
        String input=switch(scenario) {
            case "navigation" -> "4\n1\n9\n2\n9\n3\n8\n";
            case "create-club" -> "1\nRobotica\n\nno\n2\nMartes 3 PM\nsi\nsi\n\n9\n";
            case "cancel-club-edit" -> "4\n1\nChanged\ncancelar\n9\n";
            case "join-club", "full-club" -> "5\n1\n1\n01/10/2026\nsi\n\n9\n";
            case "decline-join" -> "5\n1\n1\n01/10/2026\nno\n\n9\n";
            case "withdraw" -> "7\n2\n01/11/2026\nCambio de intereses\nsi\n\n9\n";
            case "club-history" -> "8\n1\n\n9\n";
            case "create-event" -> "1\nFeria\n\n1\n01/11/2026\nAuditorio\nsi\nno\nsi\nsi\n\n9\n";
            case "competition" -> "5\n1\n1\n2\nsi\n1\n\nsi\n\n9\n";
            case "future-registration" -> "5\n1\n1\n2\n\nsi\n\n9\n";
            case "noncompetition" -> "5\n1\n1\n3\n\nsi\n\n9\n";
            case "correct-result" -> "7\n3\nno\nsi\n2\nActualizado\nsi\n\n9\n";
            case "cancel-result" -> "7\n3\nno\ncancelar\n9\n";
            case "calendar" -> "2\n01/01/2026\n31/12/2026\n\n9\n";
            case "event-history" -> "8\n1\n\n9\n";
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream output=new ByteArrayOutputStream();PrintStream console=System.out,errors=System.err;
        try(PrintStream capture=new PrintStream(output,true,StandardCharsets.UTF_8)) {
            System.setOut(capture);System.setErr(capture);
            if(scenario.equals("navigation")) new MainMenuView(new StudentView(),new TeacherView(),new AcademicView(),new ExtracurricularView(clubView,eventView)).start();
            else if(Set.of("create-club","cancel-club-edit","join-club","full-club","decline-join","withdraw","club-history").contains(scenario)) clubView.start();
            else eventView.start();
        } finally { System.setOut(console);System.setErr(errors); }
        String text=output.toString(StandardCharsets.UTF_8);
        boolean saved=Set.of("create-club","join-club","withdraw","create-event","competition","future-registration","noncompetition","correct-result").contains(scenario);
        check(writes[0]==(saved ? 1 : 0),"Unexpected writes");check(club.getName().equals("Robotica") && original.getPosition()==EventPosition.FIRST,"View mutated original during edit");
        if(scenario.startsWith("cancel")) check(text.contains("Operacion cancelada"),"Cancellation missing");
        if(scenario.equals("full-club")) check(text.contains("Sin cupos"),"Capacity error missing");
        if(scenario.endsWith("history")) check(text.contains("Ana Perez"),"Student history missing");
        if(scenario.equals("calendar")) check(text.contains("Feria"),"Calendar missing");
        if(scenario.equals("navigation")) check(text.contains("Hasta pronto"),"Main menu exit failed");
        System.out.println("Passed extracurricular view scenario: "+scenario);
    }
}
