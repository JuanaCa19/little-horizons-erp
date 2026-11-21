package view;

import controller.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.ValidationException;

/** Run each scenario in a fresh JVM to isolate the shared console scanner. */
public class FinanceViewFlowTest {
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        String scenario=args[0];int[] writes={0};
        Student student=new Student();student.setId(1);student.setFirstName("Ana");student.setFirstLastName("Perez");
        Guardian guardian=new Guardian();guardian.setId(1);guardian.setFirstName("Maria");guardian.setFirstLastName("Gil");
        PaymentConcept concept=new PaymentConcept();concept.setId(1);concept.setCode("MAT");concept.setName("Matricula");concept.setBaseAmount(new BigDecimal("350000.00"));concept.setVatRate(BigDecimal.ZERO);concept.setActive(true);
        StudentCharge charge=new StudentCharge();charge.setId(1);charge.setStudentId(1);charge.setConceptId(1);charge.setAmount(new BigDecimal("315000.00"));charge.setAcademicYear(2026);charge.setMonth(10);charge.setPeriod(PaymentPeriod.FIRST);charge.setDueDate(LocalDate.of(2026,10,1));charge.setStatus(ChargeStatus.PENDING);
        FinanceController finance=new FinanceController() {
            @Override public PaymentConcept getConcept(Integer id) { return concept; }
            @Override public int saveConcept(PaymentConcept value) { check(value.getCode().equals("MAT") && value.getBaseAmount().compareTo(new BigDecimal("350000"))==0,"Concept form");writes[0]++;return 10; }
            @Override public int savePrice(StudentPrice value) { check(value.getStudentId()==1 && value.getAmount().compareTo(new BigDecimal("315000"))==0 && value.getValidUntil()!=null,"Price form");writes[0]++;return 20; }
            @Override public BigDecimal previewChargeAmount(Integer studentId,Integer conceptId,int year,int month) { return new BigDecimal("315000.00"); }
            @Override public int createCharge(StudentCharge value) { check(value.getStudentId()==1 && value.getAmount().compareTo(new BigDecimal("315000"))==0,"Charge preview");writes[0]++;return 30; }
            @Override public List<ChargeQuote> quoteCharges(List<Integer> ids,Integer guardianId) {
                if(scenario.equals("payment-error")) throw new ValidationException("No responsable");
                check(ids.equals(List.of(1)) && guardianId==1,"Quote selection");return List.of(new ChargeQuote(charge,"Ana Perez","Matricula",BigDecimal.ZERO,BigDecimal.ZERO,charge.getAmount()));
            }
            @Override public PaymentOutcome payCharges(List<Integer> ids,Integer guardianId,PaymentMethod method,LocalDate date) { check(method==PaymentMethod.CASH,"Payment method");writes[0]++;Payment payment=new Payment();payment.setId(1);payment.setStudentId(1);payment.setGuardianId(1);payment.setConceptId(1);payment.setAmount(charge.getAmount());payment.setDate(date);payment.setMethod(method);payment.setReceiptNumber("REC-TEST");return new PaymentOutcome(List.of(payment), quoteCharges(ids, guardianId)); }
            @Override public List<StudentCharge> findOverdueCharges(LocalDate date) { return List.of(charge); }
        };
        StudentController students=new StudentController() { @Override public Student getById(Integer id) { return student; } };
        GuardianController guardians=new GuardianController() { @Override public Guardian getById(Integer id) { return guardian; } };
        FinanceView view=new FinanceView(finance,students,guardians);
        String date=LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        String input=switch(scenario) {
            case "navigation" -> "5\n1\n5\n2\n4\n3\n5\n4\n4\n6\n8\n";
            case "concept" -> "1\n1\nMAT\nMatricula\n\n350000\n0\nno\nsi\nsi\n\n5\n5\n";
            case "cancel-edit" -> "1\n4\n1\nChanged\ncancelar\n5\n5\n";
            case "price" -> "2\n1\n1\n1\n315000\nHermanos\n01/01/2026\nsi\n31/12/2026\nsi\n\n4\n5\n";
            case "charge" -> "3\n1\n1\n1\n2026\n1\n10\n01/10/2026\nsi\n\n5\n5\n";
            case "pay" -> "4\n1\n1\n1\n1\n1\n"+date+"\nsi\n\n4\n5\n";
            case "decline-payment" -> "4\n1\n1\n1\n1\n1\n"+date+"\nno\n\n4\n5\n";
            case "payment-error" -> "4\n1\n1\n1\n1\n\n4\n5\n";
            case "overdue" -> "3\n4\n"+date+"\n\n5\n5\n";
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream output=new ByteArrayOutputStream();PrintStream console=System.out,errors=System.err;
        try(PrintStream capture=new PrintStream(output,true,StandardCharsets.UTF_8)) {
            System.setOut(capture);System.setErr(capture);
            if(scenario.equals("navigation")) new MainMenuView(new StudentView(),new TeacherView(),new AcademicView(),new ExtracurricularView(),view).start();else view.start();
        } finally { System.setOut(console);System.setErr(errors); }
        String text=output.toString(StandardCharsets.UTF_8);
        check(writes[0]==(Set.of("concept","price","charge","pay").contains(scenario)?1:0),"Unexpected writes");
        check(concept.getCode().equals("MAT"),"Cancelled edit mutated source");
        if(scenario.equals("cancel-edit")) check(text.contains("Operacion cancelada"),"Cancellation missing");
        if(scenario.equals("payment-error")) check(text.contains("No responsable"),"Validation error missing");
        if(scenario.equals("pay")) check(text.contains("Pago confirmado") && text.contains("Comprobante de pago") && text.contains("REC-TEST") && text.contains("Subtotal COP") && text.contains("IVA COP"),"Payment confirmation must show receipt");
        if(scenario.equals("overdue")) check(text.contains("Ana Perez"),"Console overdue report missing");
        if(scenario.equals("navigation")) check(text.contains("Hasta pronto"),"Cannot exit main menu");
        System.out.println("Passed finance view scenario: "+scenario);
    }
}
