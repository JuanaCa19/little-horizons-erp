package integration;

import dao.*;
import dao.impl.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import service.FinanceService;
import util.exception.*;

public class FinanceModuleIntegrationTest {
    private static int checks;
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message);checks++; }
    private static void rejected(Runnable work) { failure(ValidationException.class,work); }
    private static void failure(Class<? extends Throwable> type,Runnable work) {
        try { work.run(); } catch(Throwable e) { check(type.isInstance(e),"Unexpected exception: "+e);return; }
        throw new AssertionError("Expected "+type.getSimpleName());
    }
    private static PaymentConcept concept(String code,String base,String rate) {
        PaymentConcept value=new PaymentConcept();value.setCode(code);value.setName(code);value.setBaseAmount(new BigDecimal(base));value.setVatRate(new BigDecimal(rate));value.setActive(true);return value;
    }
    private static StudentPrice price(int conceptId,LocalDate from,LocalDate until,String amount) {
        StudentPrice value=new StudentPrice();value.setStudentId(1);value.setConceptId(conceptId);value.setValidFrom(from);value.setValidUntil(until);value.setAmount(new BigDecimal(amount));value.setReason("Hermanos");return value;
    }
    private static StudentCharge charge(int studentId,int conceptId,int month,LocalDate due) {
        StudentCharge value=new StudentCharge();value.setStudentId(studentId);value.setConceptId(conceptId);value.setAcademicYear(LocalDate.now().getYear());value.setMonth(month);value.setDueDate(due);value.setPeriod(PaymentPeriod.FIRST);return value;
    }
    public static void main(String[] args) throws Exception {
        LocalDate today=LocalDate.now();int year=today.getYear();
        try(TemporaryDatabase db=new TemporaryDatabase("estudiantes","acudientes","estudiante_acudiente","conceptos_pago","precios_estudiante","cargos_estudiante","pagos")) {
            db.execute("INSERT INTO estudiantes(id_estudiante,tipo_documento,numero_documento,primer_nombre,primer_apellido,fecha_nacimiento,genero,fecha_ingreso,estado) VALUES(1,'TI','111','Ana','Perez','2010-01-01','F','2020-01-01','Activo'),(2,'TI','222','Luis','Ruiz','2010-01-01','M','2020-01-01','Activo')");
            db.execute("INSERT INTO acudientes(id_acudiente,tipo_documento,numero_documento,primer_nombre,primer_apellido,telefono_principal,direccion) VALUES(1,'CC','100','Maria','Gil','3100000000','Bucaramanga'),(2,'CC','200','Jose','Diaz','3100000001','Bucaramanga')");
            db.execute("INSERT INTO estudiante_acudiente(id_estudiante,id_acudiente,parentesco,es_principal,es_responsable_financiero) VALUES(1,1,'Madre',1,1),(2,1,'Madre',1,1),(1,2,'Padre',0,0)");
            FinanceDAO dao=new FinanceDAOImpl();
            FinanceService service=new FinanceService(dao,db.connections());
            PaymentConcept pension=concept(" pension ","350000.00","0");int pensionId=service.saveConcept(pension);
            PaymentConcept uniform=concept("UNIFORM","250000.00","19");int uniformId=service.saveConcept(uniform);
            check(pension.getCode().equals("PENSION") && service.findConcepts().size()==2,"Concept normalization and mapping");
            rejected(() -> service.saveConcept(concept("PENSION","100","0")));
            rejected(() -> service.saveConcept(concept("BAD","-1","0")));
            rejected(() -> service.saveConcept(concept("BAD","1.001","0")));
            rejected(() -> service.saveConcept(concept("BAD","1","101")));
            StudentPrice special=price(pensionId,LocalDate.of(year,1,1),LocalDate.of(year,12,31),"315000.00");int priceId=service.savePrice(special);
            check(service.getPrice(priceId).getAmount().compareTo(new BigDecimal("315000"))==0 && service.findPrices(1).size()==1,"Personalized price mapping");
            rejected(() -> service.savePrice(price(pensionId,LocalDate.of(year,6,1),null,"300000")));
            check(service.previewChargeAmount(1,pensionId,year,1).compareTo(new BigDecimal("315000"))==0,"Price effective on charge month");
            StudentCharge first=charge(1,pensionId,1,today.minusMonths(2));int firstId=service.createCharge(first);
            StudentCharge second=charge(1,uniformId,1,today);int secondId=service.createCharge(second);
            StudentCharge sibling=charge(2,pensionId,1,today);int siblingId=service.createCharge(sibling);
            check(service.getCharge(firstId).getAmount().compareTo(new BigDecimal("315000"))==0,"Charge copies personalized base");
            check(service.getCharge(siblingId).getAmount().compareTo(new BigDecimal("350000"))==0,"Other student uses base amount");
            rejected(() -> service.createCharge(charge(1,pensionId,1,today)));
            special.setAmount(new BigDecimal("300000"));service.savePrice(special);
            check(service.getCharge(firstId).getAmount().compareTo(new BigDecimal("315000"))==0,"Price edits do not rewrite existing charges");
            check(service.findOverdueCharges(today).size()==1,"Pending debt older than a month");
            uniform.setVatRate(new BigDecimal("20"));rejected(() -> service.saveConcept(uniform));uniform.setVatRate(new BigDecimal("19"));
            rejected(() -> service.quoteCharges(List.of(firstId),2));
            rejected(() -> service.quoteCharges(List.of(firstId,firstId),1));
            List<ChargeQuote> quote=service.quoteCharges(List.of(firstId,secondId,siblingId),1);
            check(quote.stream().map(ChargeQuote::total).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(new BigDecimal("962500"))==0,"Quote includes precise IVA and siblings");
            PaymentOutcome paid=service.payCharges(List.of(firstId,secondId,siblingId),1,PaymentMethod.CREDIT_CARD,today);
            check(paid.total().compareTo(new BigDecimal("962500.00"))==0,"Payment total includes VAT and siblings");
            check(paid.payments().size()==3 && paid.payments().stream().map(Payment::getReceiptNumber).distinct().count()==3,"Unique receipts for payments");
            check(service.findPaymentsByStudent(1).size()==2 && service.findPaymentsByGuardian(1).size()==3,"Student and guardian histories");
            check(service.getCharge(firstId).getStatus()==ChargeStatus.PAID && service.getCharge(firstId).getPaymentId()!=null,"Charges settled atomically");
            rejected(() -> service.payCharges(List.of(firstId),1,PaymentMethod.CASH,today));
            rejected(() -> service.cancelCharge(firstId));
            check(service.findOverdueCharges(today).isEmpty(),"Paid charges are not overdue");
            StudentCharge failureCharge=charge(1,pensionId,2,today);int failureId=service.createCharge(failureCharge);
            FinanceDAO failingDAO=(FinanceDAO)Proxy.newProxyInstance(FinanceDAO.class.getClassLoader(),new Class<?>[]{FinanceDAO.class},(proxy,method,values) -> {
                if(method.getName().equals("settleCharge")) throw new DataAccessException("Simulated settlement failure");
                try { return method.invoke(dao,values); } catch(InvocationTargetException e) { throw e.getCause(); }
            });
            FinanceService failingService=new FinanceService(failingDAO,db.connections());
            failure(DataAccessException.class,() -> failingService.payCharges(List.of(failureId),1,PaymentMethod.CASH,today));
            check(service.getCharge(failureId).getStatus()==ChargeStatus.PENDING && service.findPaymentsByGuardian(1).size()==3,"Settlement failure rolls back payment");
            service.cancelCharge(failureId);rejected(() -> service.payCharges(List.of(failureId),1,PaymentMethod.CASH,today));
            check(service.getCharge(failureId).getStatus()==ChargeStatus.CANCELLED,"Cancel pending obligation");
            PaymentConcept free=concept("FREE","0","0");int freeId=service.saveConcept(free);StudentCharge zero=charge(1,freeId,1,today.minusMonths(2));service.createCharge(zero);
            rejected(() -> service.payCharges(List.of(zero.getId()),1,PaymentMethod.CASH,today));
            check(service.findOverdueCharges(today).isEmpty(),"Zero charges are not delinquent debts");
            StudentCharge boundary=charge(1,pensionId,3,today.minusMonths(1));service.createCharge(boundary);
            check(service.findOverdueCharges(today).isEmpty(),"Exactly one month is not more than a month overdue");
            rejected(() -> service.payCharges(List.of(boundary.getId()),1,PaymentMethod.CASH,today.plusDays(1)));
            check(service.findCharges(1).size()==5,"Charge history retains paid cancelled zero and pending obligations");
        }
        System.out.println("Passed "+checks+" console finance checks using temporary MySQL tables.");
    }
}
