package dao.impl;

import dao.FinanceDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;

public class FinanceDAOImpl implements FinanceDAO {

    private PaymentConcept mapPaymentConcept(ResultSet rs) throws SQLException {
        PaymentConcept value=new PaymentConcept();
        value.setId(rs.getInt("id_concepto"));
        value.setCode(rs.getString("codigo_concepto"));
        value.setName(rs.getString("nombre_concepto"));
        value.setDescription(rs.getString("descripcion"));
        value.setBaseAmount(rs.getBigDecimal("monto_base"));
        value.setVatRate(rs.getBigDecimal("porcentaje_iva"));
        value.setRecurring(rs.getBoolean("es_recurrente"));
        value.setActive(rs.getBoolean("esta_activo"));
        return value;
    }

    private void bindPaymentConcept(PreparedStatement ps,PaymentConcept value) throws SQLException {
        ps.setString(1,value.getCode());
        ps.setString(2,value.getName());
        ps.setString(3,value.getDescription());
        ps.setBigDecimal(4,value.getBaseAmount());
        ps.setBigDecimal(5,value.getVatRate());
        ps.setBoolean(6,value.isRecurring());
        ps.setBoolean(7,value.isActive());
    }

    @Override public int createPaymentConcept(PaymentConcept value,Connection c) {
        return JdbcSupport.insert(c,"INSERT INTO conceptos_pago (codigo_concepto,nombre_concepto,descripcion,monto_base,porcentaje_iva,es_recurrente,esta_activo) VALUES (?,?,?,?,?,?,?)",ps -> bindPaymentConcept(ps,value));
    }
    @Override public Optional<PaymentConcept> findPaymentConcept(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM conceptos_pago WHERE id_concepto=? FOR UPDATE",ps -> ps.setInt(1,id),this::mapPaymentConcept).stream().findFirst();
    }
    @Override public void updatePaymentConcept(PaymentConcept value,Connection c) {
        JdbcSupport.update(c,"UPDATE conceptos_pago SET codigo_concepto=?,nombre_concepto=?,descripcion=?,monto_base=?,porcentaje_iva=?,es_recurrente=?,esta_activo=? WHERE id_concepto=?",ps -> { bindPaymentConcept(ps,value);ps.setInt(8,value.getId()); });
    }

    private StudentPrice mapStudentPrice(ResultSet rs) throws SQLException {
        StudentPrice value=new StudentPrice();
        value.setId(rs.getInt("id_precio"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setConceptId(rs.getInt("id_concepto"));
        value.setAmount(rs.getBigDecimal("monto_personalizado"));
        value.setReason(rs.getString("motivo"));
        value.setValidFrom(rs.getObject("vigente_desde",LocalDate.class));
        value.setValidUntil(rs.getObject("vigente_hasta",LocalDate.class));
        return value;
    }

    private void bindStudentPrice(PreparedStatement ps,StudentPrice value) throws SQLException {
        ps.setInt(1,value.getStudentId());
        ps.setInt(2,value.getConceptId());
        ps.setBigDecimal(3,value.getAmount());
        ps.setString(4,value.getReason());
        ps.setObject(5,value.getValidFrom(),Types.DATE);
        ps.setObject(6,value.getValidUntil(),Types.DATE);
    }

    @Override public int createStudentPrice(StudentPrice value,Connection c) {
        return JdbcSupport.insert(c,"INSERT INTO precios_estudiante (id_estudiante,id_concepto,monto_personalizado,motivo,vigente_desde,vigente_hasta) VALUES (?,?,?,?,?,?)",ps -> bindStudentPrice(ps,value));
    }
    @Override public Optional<StudentPrice> findStudentPrice(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM precios_estudiante WHERE id_precio=? FOR UPDATE",ps -> ps.setInt(1,id),this::mapStudentPrice).stream().findFirst();
    }
    @Override public void updateStudentPrice(StudentPrice value,Connection c) {
        JdbcSupport.update(c,"UPDATE precios_estudiante SET id_estudiante=?,id_concepto=?,monto_personalizado=?,motivo=?,vigente_desde=?,vigente_hasta=? WHERE id_precio=?",ps -> { bindStudentPrice(ps,value);ps.setInt(7,value.getId()); });
    }

    private StudentCharge mapStudentCharge(ResultSet rs) throws SQLException {
        StudentCharge value=new StudentCharge();
        value.setId(rs.getInt("id_cargo"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setConceptId(rs.getInt("id_concepto"));
        value.setAmount(rs.getBigDecimal("monto_cargo"));
        value.setAcademicYear(rs.getInt("anio_academico"));
        value.setPeriod(PaymentPeriod.fromDbValue(rs.getString("periodo_academico")));
        value.setMonth(rs.getInt("mes_cargo"));
        value.setDueDate(rs.getObject("fecha_vencimiento",LocalDate.class));
        value.setStatus(ChargeStatus.fromDbValue(rs.getString("estado")));
        value.setPaymentId(rs.getObject("id_pago",Integer.class));
        return value;
    }

    private void bindStudentCharge(PreparedStatement ps,StudentCharge value) throws SQLException {
        ps.setInt(1,value.getStudentId());
        ps.setInt(2,value.getConceptId());
        ps.setBigDecimal(3,value.getAmount());
        ps.setInt(4,value.getAcademicYear());
        ps.setString(5,value.getPeriod().getDbValue());
        ps.setInt(6,value.getMonth());
        ps.setObject(7,value.getDueDate(),Types.DATE);
        ps.setString(8,value.getStatus().getDbValue());
        ps.setObject(9,value.getPaymentId(),Types.INTEGER);
    }

    @Override public int createStudentCharge(StudentCharge value,Connection c) {
        return JdbcSupport.insert(c,"INSERT INTO cargos_estudiante (id_estudiante,id_concepto,monto_cargo,anio_academico,periodo_academico,mes_cargo,fecha_vencimiento,estado,id_pago) VALUES (?,?,?,?,?,?,?,?,?)",ps -> bindStudentCharge(ps,value));
    }
    @Override public Optional<StudentCharge> findStudentCharge(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM cargos_estudiante WHERE id_cargo=? FOR UPDATE",ps -> ps.setInt(1,id),this::mapStudentCharge).stream().findFirst();
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment value=new Payment();
        value.setId(rs.getInt("id_pago"));
        value.setStudentId(rs.getInt("id_estudiante"));
        value.setGuardianId(rs.getInt("id_acudiente"));
        value.setConceptId(rs.getInt("id_concepto"));
        value.setAmount(rs.getBigDecimal("monto_pagado"));
        value.setDate(rs.getObject("fecha_pago",LocalDate.class));
        value.setMethod(PaymentMethod.fromDbValue(rs.getString("metodo_pago")));
        value.setReceiptNumber(rs.getString("numero_recibo"));
        value.setAcademicYear(rs.getInt("anio_academico"));
        value.setPeriod(PaymentPeriod.fromDbValue(rs.getString("periodo_academico")));
        value.setNotes(rs.getString("notas"));
        return value;
    }

    private void bindPayment(PreparedStatement ps,Payment value) throws SQLException {
        ps.setInt(1,value.getStudentId());
        ps.setInt(2,value.getGuardianId());
        ps.setInt(3,value.getConceptId());
        ps.setBigDecimal(4,value.getAmount());
        ps.setObject(5,value.getDate(),Types.DATE);
        ps.setString(6,value.getMethod().getDbValue());
        ps.setString(7,value.getReceiptNumber());
        ps.setInt(8,value.getAcademicYear());
        ps.setString(9,value.getPeriod().getDbValue());
        ps.setString(10,value.getNotes());
    }

    @Override public int createPayment(Payment value,Connection c) {
        return JdbcSupport.insert(c,"INSERT INTO pagos (id_estudiante,id_acudiente,id_concepto,monto_pagado,fecha_pago,metodo_pago,numero_recibo,anio_academico,periodo_academico,notas) VALUES (?,?,?,?,?,?,?,?,?,?)",ps -> bindPayment(ps,value));
    }
    @Override public Optional<Payment> findPayment(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM pagos WHERE id_pago=?",ps -> ps.setInt(1,id),this::mapPayment).stream().findFirst();
    }

    @Override public Optional<Student> findStudent(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM estudiantes WHERE id_estudiante=? FOR UPDATE",ps -> ps.setInt(1,id),rs -> {
            Student value=new Student();value.setId(rs.getInt("id_estudiante"));value.setFirstName(rs.getString("primer_nombre"));value.setMiddleName(rs.getString("segundo_nombre"));
            value.setFirstLastName(rs.getString("primer_apellido"));value.setSecondLastName(rs.getString("segundo_apellido"));
            value.setStatus(StudentStatus.fromDbValue(rs.getString("estado")));value.setAdmissionDate(rs.getObject("fecha_ingreso",LocalDate.class));return value;
        }).stream().findFirst();
    }
    @Override public Optional<Guardian> findGuardian(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM acudientes WHERE id_acudiente=?",ps -> ps.setInt(1,id),GuardianDAOImpl::mapRow).stream().findFirst();
    }
    @Override public boolean isFinancialResponsible(Integer studentId,Integer guardianId,Connection c) {
        return !JdbcSupport.query(c,"SELECT id_acudiente FROM estudiante_acudiente WHERE id_estudiante=? AND id_acudiente=? AND es_responsable_financiero=TRUE",
                ps -> { ps.setInt(1,studentId);ps.setInt(2,guardianId); },rs -> rs.getInt(1)).isEmpty();
    }
    @Override public Optional<PaymentConcept> findConceptByCode(String code,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM conceptos_pago WHERE codigo_concepto=?",ps -> ps.setString(1,code),this::mapPaymentConcept).stream().findFirst();
    }
    @Override public List<PaymentConcept> findConcepts(Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM conceptos_pago ORDER BY nombre_concepto",ps -> {},this::mapPaymentConcept);
    }
    @Override public boolean hasCharges(Integer conceptId,Connection c) {
        return !JdbcSupport.query(c,"SELECT id_cargo FROM cargos_estudiante WHERE id_concepto=? LIMIT 1",ps -> ps.setInt(1,conceptId),rs -> rs.getInt(1)).isEmpty();
    }
    @Override public Optional<StudentPrice> findEffectivePrice(Integer studentId,Integer conceptId,LocalDate date,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM precios_estudiante WHERE id_estudiante=? AND id_concepto=? AND vigente_desde<=? AND (vigente_hasta IS NULL OR vigente_hasta>=?) ORDER BY vigente_desde DESC LIMIT 1",
                ps -> { ps.setInt(1,studentId);ps.setInt(2,conceptId);ps.setObject(3,date,Types.DATE);ps.setObject(4,date,Types.DATE); },this::mapStudentPrice).stream().findFirst();
    }
    @Override public boolean hasPriceOverlap(StudentPrice value,Connection c) {
        return !JdbcSupport.query(c,"SELECT id_precio FROM precios_estudiante WHERE id_estudiante=? AND id_concepto=? AND vigente_desde<=? AND (vigente_hasta IS NULL OR vigente_hasta>=?) AND id_precio<>?",
                ps -> { ps.setInt(1,value.getStudentId());ps.setInt(2,value.getConceptId());ps.setObject(3,value.getValidUntil()==null ? LocalDate.of(9999,12,31) : value.getValidUntil(),Types.DATE);
                    ps.setObject(4,value.getValidFrom(),Types.DATE);ps.setInt(5,value.getId()==null ? 0 : value.getId()); },rs -> rs.getInt(1)).isEmpty();
    }
    @Override public List<StudentPrice> findPrices(Integer studentId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM precios_estudiante WHERE id_estudiante=? ORDER BY id_concepto,vigente_desde",ps -> ps.setInt(1,studentId),this::mapStudentPrice);
    }
    @Override public Optional<StudentCharge> findChargeByKey(Integer studentId,Integer conceptId,int year,int month,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM cargos_estudiante WHERE id_estudiante=? AND id_concepto=? AND anio_academico=? AND mes_cargo=?",
                ps -> { ps.setInt(1,studentId);ps.setInt(2,conceptId);ps.setInt(3,year);ps.setInt(4,month); },this::mapStudentCharge).stream().findFirst();
    }
    @Override public List<StudentCharge> findCharges(Integer studentId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM cargos_estudiante WHERE id_estudiante=? ORDER BY fecha_vencimiento,id_cargo",ps -> ps.setInt(1,studentId),this::mapStudentCharge);
    }
    @Override public List<StudentCharge> findOverdueCharges(LocalDate cutoff,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM cargos_estudiante WHERE estado='Pendiente' AND monto_cargo>0 AND fecha_vencimiento<? ORDER BY fecha_vencimiento,id_estudiante",ps -> ps.setObject(1,cutoff,Types.DATE),this::mapStudentCharge);
    }
    @Override public void settleCharge(Integer chargeId,Integer paymentId,Connection c) {
        JdbcSupport.update(c,"UPDATE cargos_estudiante SET estado='Pagado',id_pago=? WHERE id_cargo=? AND estado='Pendiente'",ps -> { ps.setInt(1,paymentId);ps.setInt(2,chargeId); });
    }
    @Override public void cancelCharge(Integer chargeId,Connection c) {
        JdbcSupport.update(c,"UPDATE cargos_estudiante SET estado='Anulado' WHERE id_cargo=? AND estado='Pendiente'",ps -> ps.setInt(1,chargeId));
    }
    @Override public List<Payment> findPaymentsByStudent(Integer studentId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM pagos WHERE id_estudiante=? ORDER BY fecha_pago,id_pago",ps -> ps.setInt(1,studentId),this::mapPayment);
    }
    @Override public List<Payment> findPaymentsByGuardian(Integer guardianId,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM pagos WHERE id_acudiente=? ORDER BY fecha_pago,id_pago",ps -> ps.setInt(1,guardianId),this::mapPayment);
    }

}
