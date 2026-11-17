package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.ResourceLoan;
import model.enums.BorrowerType;

public interface LoanDAO {
    int create(ResourceLoan value,Connection connection);
    Optional<ResourceLoan> findById(BorrowerType type,Integer id,Connection connection);
    boolean hasOpenLoan(Integer resourceId,Connection connection);
    Optional<LocalDate> latestReturnDate(Integer resourceId,Connection connection);
    LocalDate requireActiveBorrower(BorrowerType type,Integer borrowerId,Connection connection);
    void markReturned(BorrowerType type,Integer id,LocalDate actualDate,Connection connection);
    List<ResourceLoan> findByResource(Integer resourceId,Connection connection);
    List<ResourceLoan> findByBorrower(BorrowerType type,Integer borrowerId,Connection connection);
    List<ResourceLoan> findOverdue(LocalDate referenceDate,Connection connection);
}
