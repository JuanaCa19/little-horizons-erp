package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.report.OverdueChargeRow;

public interface FinanceDAO {
    int createPaymentConcept(PaymentConcept value,Connection c);
    Optional<PaymentConcept> findPaymentConcept(Integer id,Connection c);
    void updatePaymentConcept(PaymentConcept value,Connection c);
    int createStudentPrice(StudentPrice value,Connection c);
    Optional<StudentPrice> findStudentPrice(Integer id,Connection c);
    void updateStudentPrice(StudentPrice value,Connection c);
    int createStudentCharge(StudentCharge value,Connection c);
    Optional<StudentCharge> findStudentCharge(Integer id,Connection c);
    int createPayment(Payment value,Connection c);
    Optional<Payment> findPayment(Integer id,Connection c);
    Optional<Student> findStudent(Integer id,Connection c);
    Optional<Guardian> findGuardian(Integer id,Connection c);
    boolean isFinancialResponsible(Integer studentId,Integer guardianId,Connection c);
    Optional<PaymentConcept> findConceptByCode(String code,Connection c);
    List<PaymentConcept> findConcepts(Connection c);
    boolean hasCharges(Integer conceptId,Connection c);
    Optional<StudentPrice> findEffectivePrice(Integer studentId,Integer conceptId,LocalDate date,Connection c);
    boolean hasPriceOverlap(StudentPrice value,Connection c);
    List<StudentPrice> findPrices(Integer studentId,Connection c);
    Optional<StudentCharge> findChargeByKey(Integer studentId,Integer conceptId,int year,int month,Connection c);
    List<StudentCharge> findCharges(Integer studentId,Connection c);
    List<StudentCharge> findOverdueCharges(LocalDate cutoff,Connection c);
    List<OverdueChargeRow> findOverduePortfolio(LocalDate cutoff, LocalDate overdueBefore, Connection c);
    void settleCharge(Integer chargeId,Integer paymentId,Connection c);
    void cancelCharge(Integer chargeId,Connection c);
    List<Payment> findPaymentsByStudent(Integer studentId,Connection c);
    List<Payment> findPaymentsByGuardian(Integer guardianId,Connection c);
}
