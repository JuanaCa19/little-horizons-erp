package dao.impl;

import dao.LoanDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.ResourceLoan;
import model.enums.*;
import util.exception.ValidationException;

public class LoanDAOImpl implements LoanDAO {
    private String table(BorrowerType type) { return switch(type) { case STUDENT -> "prestamos_estudiante";case TEACHER -> "prestamos_profesor"; }; }
    private String borrowerColumn(BorrowerType type) { return type==BorrowerType.STUDENT ? "id_estudiante" : "id_profesor"; }
    private String projection(BorrowerType type) {
        return "SELECT id_prestamo,id_recurso,"+borrowerColumn(type)+" AS borrower_id,'"+type.name()+"' AS borrower_type,fecha_prestamo,fecha_devolucion_esperada,fecha_devolucion_real,estado_prestamo,observaciones FROM "+table(type);
    }
    private String allLoans() { return "("+projection(BorrowerType.STUDENT)+" UNION ALL "+projection(BorrowerType.TEACHER)+") loans"; }
    private ResourceLoan mapRow(ResultSet rs) throws SQLException {
        ResourceLoan value=new ResourceLoan();value.setId(rs.getInt("id_prestamo"));value.setResourceId(rs.getInt("id_recurso"));value.setBorrowerId(rs.getInt("borrower_id"));
        value.setBorrowerType(BorrowerType.valueOf(rs.getString("borrower_type")));value.setLoanDate(rs.getObject("fecha_prestamo",LocalDate.class));
        value.setExpectedReturnDate(rs.getObject("fecha_devolucion_esperada",LocalDate.class));value.setActualReturnDate(rs.getObject("fecha_devolucion_real",LocalDate.class));
        value.setStatus(LoanStatus.fromDbValue(rs.getString("estado_prestamo")));value.setNotes(rs.getString("observaciones"));return value;
    }
    @Override public int create(ResourceLoan value,Connection c) {
        // Table and borrower column names come exclusively from the BorrowerType enum.
        return JdbcSupport.insert(c,"INSERT INTO "+table(value.getBorrowerType())+" (id_recurso,"+borrowerColumn(value.getBorrowerType())+",fecha_prestamo,fecha_devolucion_esperada,fecha_devolucion_real,estado_prestamo,observaciones) VALUES (?,?,?,?,?,?,?)",ps -> {
            ps.setInt(1,value.getResourceId());ps.setInt(2,value.getBorrowerId());ps.setObject(3,value.getLoanDate(),Types.DATE);ps.setObject(4,value.getExpectedReturnDate(),Types.DATE);
            ps.setObject(5,value.getActualReturnDate(),Types.DATE);ps.setString(6,value.getStatus().getDbValue());ps.setString(7,value.getNotes());
        });
    }
    @Override public Optional<ResourceLoan> findById(BorrowerType type,Integer id,Connection c) {
        return JdbcSupport.query(c,projection(type)+" WHERE id_prestamo=?",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst();
    }
    @Override public boolean hasOpenLoan(Integer resourceId,Connection c) {
        String sql="SELECT EXISTS(SELECT 1 FROM prestamos_estudiante WHERE id_recurso=? AND estado_prestamo IN ('Activo','Vencido')) OR EXISTS(SELECT 1 FROM prestamos_profesor WHERE id_recurso=? AND estado_prestamo IN ('Activo','Vencido'))";
        return JdbcSupport.query(c,sql,ps -> { ps.setInt(1,resourceId);ps.setInt(2,resourceId); },rs -> rs.getBoolean(1)).getFirst();
    }
    @Override public Optional<LocalDate> latestReturnDate(Integer resourceId,Connection c) {
        List<LocalDate> values=JdbcSupport.query(c,"SELECT MAX(fecha_devolucion_real) last_return FROM "+allLoans()+" WHERE id_recurso=? AND estado_prestamo='Devuelto'",ps -> ps.setInt(1,resourceId),rs -> rs.getObject("last_return",LocalDate.class));
        return Optional.ofNullable(values.getFirst());
    }
    @Override public LocalDate requireActiveBorrower(BorrowerType type,Integer borrowerId,Connection c) {
        String source=type==BorrowerType.STUDENT ? "estudiantes" : "profesores";
        String date=type==BorrowerType.STUDENT ? "fecha_ingreso" : "fecha_contratacion";
        return JdbcSupport.query(c,"SELECT "+date+" FROM "+source+" WHERE "+borrowerColumn(type)+"=? AND estado='Activo' FOR UPDATE",ps -> ps.setInt(1,borrowerId),rs -> rs.getObject(1,LocalDate.class))
                .stream().findFirst().orElseThrow(() -> new ValidationException("El solicitante no existe o no esta activo."));
    }
    @Override public void markReturned(BorrowerType type,Integer id,LocalDate date,Connection c) {
        JdbcSupport.update(c,"UPDATE "+table(type)+" SET estado_prestamo='Devuelto',fecha_devolucion_real=? WHERE id_prestamo=? AND estado_prestamo IN ('Activo','Vencido')",ps -> { ps.setObject(1,date,Types.DATE);ps.setInt(2,id); });
    }
    @Override public List<ResourceLoan> findByResource(Integer resourceId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM "+allLoans()+" WHERE id_recurso=? ORDER BY fecha_prestamo,borrower_type,id_prestamo",ps -> ps.setInt(1,resourceId),this::mapRow);
    }
    @Override public List<ResourceLoan> findByBorrower(BorrowerType type,Integer borrowerId,Connection c) {
        return JdbcSupport.query(c,projection(type)+" WHERE "+borrowerColumn(type)+"=? ORDER BY fecha_prestamo,id_prestamo",ps -> ps.setInt(1,borrowerId),this::mapRow);
    }
    @Override public List<ResourceLoan> findOverdue(LocalDate date,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM "+allLoans()+" WHERE estado_prestamo IN ('Activo','Vencido') AND fecha_devolucion_esperada<? ORDER BY fecha_devolucion_esperada,borrower_type,id_prestamo",ps -> ps.setObject(1,date,Types.DATE),this::mapRow);
    }
}
