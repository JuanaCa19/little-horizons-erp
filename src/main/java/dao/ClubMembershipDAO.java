package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.ClubMembership;

public interface ClubMembershipDAO {
    Optional<ClubMembership> findById(Integer id,Connection connection);
    List<ClubMembership> findByStudentId(Integer studentId);
    List<ClubMembership> findByClubId(Integer clubId);
    Optional<ClubMembership> findActive(Integer studentId,Integer clubId,Connection connection);
    Optional<ClubMembership> findLatest(Integer studentId,Integer clubId,Connection connection);
    int countActive(Integer clubId,Connection connection);
    void withdraw(Integer id,LocalDate date,String reason,Connection connection);
    int create(ClubMembership value,Connection connection);
}
