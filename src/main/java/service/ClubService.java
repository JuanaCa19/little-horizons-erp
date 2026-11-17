package service;

import dao.*;
import dao.impl.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.MembershipStatus;
import util.ConnectionDB;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class ClubService {
    private final ClubDAO clubs;
    private final ClubMembershipDAO memberships;
    private final ExtracurricularContextDAO context;
    private final TransactionManager transactions;
    public ClubService() { this(new ClubDAOImpl(),new ClubMembershipDAOImpl(),new ExtracurricularContextDAOImpl(),ConnectionDB::getConnection); }
    public ClubService(ClubDAO clubs,ClubMembershipDAO memberships,ExtracurricularContextDAO context,Supplier<Connection> connections) {
        this.clubs=Objects.requireNonNull(clubs);this.memberships=Objects.requireNonNull(memberships);
        this.context=Objects.requireNonNull(context);this.transactions=new TransactionManager(connections);
    }
    private void validate(Club value) {
        require(value!=null,"El club es obligatorio.");value.setName(text(value.getName(),"El nombre"));
        require(value.getName().length()<=100,"El nombre admite hasta 100 caracteres.");
        require(value.getCapacity()>0,"El cupo debe ser mayor que cero.");
        require(value.getMeetingSchedule()==null || value.getMeetingSchedule().length()<=100,"El horario admite hasta 100 caracteres.");
        if(value.getSupervisorId()!=null) id(value.getSupervisorId());
        clubs.findByName(value.getName()).ifPresent(existing -> require(value.getId()!=null && Objects.equals(value.getId(),existing.getId()),"Ya existe un club con ese nombre."));
    }
    public int create(Club value) {
        require(value!=null && value.getId()==null,"El nuevo club no debe tener identificador.");validate(value);
        int id=transactions.execute(c -> {
            if(value.getSupervisorId()!=null) context.requireActiveTeacher(value.getSupervisorId(),c);
            return clubs.create(value,c);
        });value.setId(id);return id;
    }
    public Club getById(Integer clubId) {
        id(clubId);return clubs.findById(clubId).orElseThrow(() -> new ValidationException("Club no encontrado."));
    }
    public List<Club> findAll() { return clubs.findAll(); }
    public void update(Club value) {
        validate(value);id(value.getId());
        transactions.execute(c -> {
            Club existing=clubs.findById(value.getId(),c).orElseThrow(() -> new ValidationException("Club no encontrado."));
            require(value.getCapacity()>=memberships.countActive(value.getId(),c),"El cupo no puede ser menor que las membresias activas.");
            if(value.getSupervisorId()!=null && !Objects.equals(existing.getSupervisorId(),value.getSupervisorId())) context.requireActiveTeacher(value.getSupervisorId(),c);
            clubs.update(value,c);return null;
        });
    }
    public int availableSeats(Integer clubId) {
        id(clubId);return transactions.execute(c -> {
            Club club=clubs.findById(clubId,c).orElseThrow(() -> new ValidationException("Club no encontrado."));
            return Math.max(0,club.getCapacity()-memberships.countActive(clubId,c));
        });
    }
    public int join(ClubMembership value) {
        require(value!=null,"La membresia es obligatoria.");require(value.getId()==null,"La nueva membresia no debe tener identificador.");
        id(value.getStudentId());id(value.getClubId());
        require(value.getEntryDate()!=null && !value.getEntryDate().isAfter(LocalDate.now()),"La fecha de ingreso no puede ser futura.");
        require(value.getStatus()==null || value.getStatus()==MembershipStatus.ACTIVE,"La nueva membresia debe estar activa.");
        require(value.getWithdrawalDate()==null && (value.getWithdrawalReason()==null || value.getWithdrawalReason().isBlank()),"Una nueva membresia no puede tener datos de retiro.");
        int id=transactions.execute(c -> {
            // Serialize membership changes and capacity changes on the same club row.
            Club club=clubs.findById(value.getClubId(),c).orElseThrow(() -> new ValidationException("Club no encontrado."));
            require(club.isActive(),"El club esta inactivo.");
            LocalDate admission=context.requireStudent(value.getStudentId(),true,c);
            require(!value.getEntryDate().isBefore(admission),"La fecha no puede ser anterior al ingreso al colegio.");
            require(memberships.findActive(value.getStudentId(),value.getClubId(),c).isEmpty(),"El estudiante ya tiene una membresia activa en este club.");
            memberships.findLatest(value.getStudentId(),value.getClubId(),c).ifPresent(previous ->
                    require(previous.getWithdrawalDate()!=null && !value.getEntryDate().isBefore(previous.getWithdrawalDate()),"El reingreso no puede ser anterior al retiro previo."));
            require(memberships.countActive(club.getId(),c)<club.getCapacity(),"El club no tiene cupos disponibles.");
            ClubMembership pending=new ClubMembership();pending.setStudentId(value.getStudentId());pending.setClubId(value.getClubId());
            pending.setEntryDate(value.getEntryDate());pending.setStatus(MembershipStatus.ACTIVE);
            return memberships.create(pending,c);
        });value.setId(id);value.setStatus(MembershipStatus.ACTIVE);return id;
    }
    public ClubMembership getMembership(Integer membershipId) {
        id(membershipId);return transactions.execute(c -> memberships.findById(membershipId,c).orElseThrow(() -> new ValidationException("Membresia no encontrada.")));
    }
    public void withdraw(Integer membershipId,LocalDate date,String reason) {
        ClubMembership snapshot=getMembership(membershipId);
        require(date!=null && !date.isAfter(LocalDate.now()),"La fecha de retiro no puede ser futura.");
        String withdrawalReason=text(reason,"El motivo de retiro");
        transactions.execute(c -> {
            clubs.findById(snapshot.getClubId(),c).orElseThrow(() -> new ValidationException("Club no encontrado."));
            ClubMembership existing=memberships.findById(membershipId,c).orElseThrow(() -> new ValidationException("Membresia no encontrada."));
            require(existing.getStatus()==MembershipStatus.ACTIVE,"La membresia ya esta retirada.");
            require(!date.isBefore(existing.getEntryDate()),"El retiro no puede ser anterior al ingreso al club.");
            memberships.withdraw(membershipId,date,withdrawalReason,c);return null;
        });
    }
    public List<ClubMembership> findMembershipsByClub(Integer clubId) { getById(clubId);return memberships.findByClubId(clubId); }
    public List<ClubMembership> findMembershipsByStudent(Integer studentId) { id(studentId);return memberships.findByStudentId(studentId); }
}
