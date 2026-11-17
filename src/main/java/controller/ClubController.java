package controller;

import java.time.LocalDate;
import java.util.*;
import model.*;
import service.ClubService;

public class ClubController {
    private final ClubService service;
    public ClubController() { this(new ClubService()); }
    public ClubController(ClubService service) { this.service=Objects.requireNonNull(service); }

    public int create(Club value) { return service.create(value); }

    public Club getById(Integer id) { return service.getById(id); }

    public List<Club> findAll() { return service.findAll(); }

    public void update(Club value) { service.update(value); }

    public int availableSeats(Integer clubId) { return service.availableSeats(clubId); }

    public int join(ClubMembership value) { return service.join(value); }

    public ClubMembership getMembership(Integer id) { return service.getMembership(id); }

    public void withdraw(Integer id,LocalDate date,String reason) { service.withdraw(id,date,reason); }

    public List<ClubMembership> findMembershipsByClub(Integer clubId) { return service.findMembershipsByClub(clubId); }

    public List<ClubMembership> findMembershipsByStudent(Integer studentId) { return service.findMembershipsByStudent(studentId); }
}
