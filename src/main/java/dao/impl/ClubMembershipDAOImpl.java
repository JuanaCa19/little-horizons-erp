package dao.impl;

import dao.ClubMembershipDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.ClubMembership;
import model.enums.*;

public class ClubMembershipDAOImpl implements ClubMembershipDAO {
    private final JdbcSupport jdbc;
    public ClubMembershipDAOImpl() { jdbc=new JdbcSupport(); }
    public ClubMembershipDAOImpl(Supplier<Connection> connections) { jdbc=new JdbcSupport(connections); }

    private ClubMembership mapRow(ResultSet rs) throws SQLException {
        ClubMembership value=new ClubMembership();
        value.setId(rs.getInt("id_membresia"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setClubId(rs.getInt("id_club"));
        value.setEntryDate(rs.getObject("fecha_ingreso",LocalDate.class));
        value.setStatus(MembershipStatus.fromDbValue(rs.getString("estado_membresia")));
        value.setWithdrawalDate(rs.getObject("fecha_retiro",LocalDate.class));
        value.setWithdrawalReason(rs.getString("motivo_retiro"));
        return value;
    }

    private void bind(PreparedStatement ps,ClubMembership value) throws SQLException {
        ps.setInt(1,value.getStudentId());
        ps.setInt(2,value.getClubId());
        ps.setObject(3,value.getEntryDate(),Types.DATE);
        ps.setString(4,value.getStatus().getDbValue());
        ps.setObject(5,value.getWithdrawalDate(),Types.DATE);
        ps.setString(6,value.getWithdrawalReason());
    }

    @Override public Optional<ClubMembership> findById(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM membresias_club WHERE id_membresia=?",ps -> ps.setInt(1,id),this::mapRow).stream().findFirst();
    }
    @Override public int create(ClubMembership value,Connection c) { return JdbcSupport.insert(c,"INSERT INTO membresias_club (id_estudiante,id_club,fecha_ingreso,estado_membresia,fecha_retiro,motivo_retiro) VALUES (?,?,?,?,?,?)",ps -> bind(ps,value)); }

    @Override public List<ClubMembership> findByStudentId(Integer studentId) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM membresias_club WHERE id_estudiante=? ORDER BY fecha_ingreso DESC,id_membresia DESC",ps -> ps.setInt(1,studentId),this::mapRow));
    }
    @Override public List<ClubMembership> findByClubId(Integer clubId) {
        return jdbc.execute(c -> JdbcSupport.query(c,"SELECT * FROM membresias_club WHERE id_club=? ORDER BY estado_membresia,id_estudiante,fecha_ingreso DESC",ps -> ps.setInt(1,clubId),this::mapRow));
    }
    @Override public Optional<ClubMembership> findActive(Integer studentId,Integer clubId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM membresias_club WHERE id_estudiante=? AND id_club=? AND estado_membresia='Activa'",ps -> { ps.setInt(1,studentId);ps.setInt(2,clubId); },this::mapRow).stream().findFirst();
    }
    @Override public Optional<ClubMembership> findLatest(Integer studentId,Integer clubId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM membresias_club WHERE id_estudiante=? AND id_club=? ORDER BY fecha_ingreso DESC,id_membresia DESC LIMIT 1",ps -> { ps.setInt(1,studentId);ps.setInt(2,clubId); },this::mapRow).stream().findFirst();
    }
    @Override public int countActive(Integer clubId,Connection c) {
        return JdbcSupport.query(c,"SELECT COUNT(*) FROM membresias_club WHERE id_club=? AND estado_membresia='Activa'",ps -> ps.setInt(1,clubId),rs -> rs.getInt(1)).getFirst();
    }
    @Override public void withdraw(Integer id,LocalDate date,String reason,Connection c) {
        JdbcSupport.update(c,"UPDATE membresias_club SET estado_membresia='Retirada',fecha_retiro=?,motivo_retiro=? WHERE id_membresia=? AND estado_membresia='Activa'",
                ps -> { ps.setObject(1,date,Types.DATE);ps.setString(2,reason);ps.setInt(3,id); });
    }

}
