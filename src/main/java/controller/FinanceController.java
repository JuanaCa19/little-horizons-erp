package controller;

import java.util.*;
import java.nio.file.Path;
import java.time.LocalDate;
import model.*;
import model.enums.PaymentMethod;
import service.FinanceService;

public class FinanceController {
    public java.math.BigDecimal previewChargeAmount(Integer studentId,Integer conceptId,int year,int month) { return service.previewChargeAmount(studentId,conceptId,year,month); }
    private final FinanceService service;
    public FinanceController() { this(new FinanceService()); }
    public FinanceController(FinanceService service) { this.service=Objects.requireNonNull(service); }

    public PaymentConcept getConcept(Integer id) { return service.getConcept(id); }

    public StudentCharge getCharge(Integer id) { return service.getCharge(id); }

    public List<PaymentConcept> findConcepts() { return service.findConcepts(); }

    public int saveConcept(PaymentConcept value) { return service.saveConcept(value); }

    public StudentPrice getPrice(Integer id) { return service.getPrice(id); }

    public List<StudentPrice> findPrices(Integer studentId) { return service.findPrices(studentId); }

    public int savePrice(StudentPrice value) { return service.savePrice(value); }

    public int createCharge(StudentCharge value) { return service.createCharge(value); }

    public void cancelCharge(Integer id) { service.cancelCharge(id); }

    public List<StudentCharge> findCharges(Integer studentId) { return service.findCharges(studentId); }

    public List<StudentCharge> findOverdueCharges(LocalDate date) { return service.findOverdueCharges(date); }

    public Path exportOverduePortfolio(LocalDate cutoff) {
        return service.exportOverduePortfolio(cutoff);
    }

    public List<ChargeQuote> quoteCharges(List<Integer> ids,Integer guardianId) { return service.quoteCharges(ids,guardianId); }

    public PaymentOutcome payCharges(List<Integer> ids,Integer guardianId,PaymentMethod method,LocalDate date) { return service.payCharges(ids,guardianId,method,date); }




    public List<Payment> findPaymentsByStudent(Integer id) { return service.findPaymentsByStudent(id); }

    public List<Payment> findPaymentsByGuardian(Integer id) { return service.findPaymentsByGuardian(id); }

}
