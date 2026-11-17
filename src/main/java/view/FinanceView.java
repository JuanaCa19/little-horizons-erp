package view;

import controller.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class FinanceView {
    private final FinanceController controller;
    private final StudentController students;
    private final GuardianController guardians;
    public FinanceView() { this(new FinanceController(),new StudentController(),new GuardianController()); }
    public FinanceView(FinanceController controller,StudentController students,GuardianController guardians) {
        this.controller=Objects.requireNonNull(controller);this.students=Objects.requireNonNull(students);this.guardians=Objects.requireNonNull(guardians);
    }
    public void start() {
        menu("Gestion Financiera","Volver al menu principal",new String[]{"Conceptos de pago","Precios personalizados y becas","Cargos pendientes y mora","Pagos e historial"},
                this::conceptMenu,this::priceMenu,this::chargeMenu,this::paymentMenu);
    }
    private int student() { Student value=students.getById(readId("ID del estudiante"));field("Estudiante",value.getFullName());return value.getId(); }
    private int guardian() { Guardian value=guardians.getById(readId("ID del acudiente"));field("Acudiente",value.getFullName());return value.getId(); }
    private int concept() { PaymentConcept value=controller.getConcept(readId("ID del concepto"));field("Concepto",value.getName());return value.getId(); }
    private BigDecimal amount(String label) { return readDecimal(label+": ",BigDecimal.ZERO,new BigDecimal("99999999.99"),2); }
    private PaymentPeriod period() { return choose("Periodo",PaymentPeriod.values(),PaymentPeriod::getDbValue,null); }
    private void conceptMenu() {
        menu("Conceptos de Pago","Volver",new String[]{"Crear concepto","Listar conceptos","Consultar detalle","Actualizar concepto"},
                action(() -> saveConcept(null)),action(this::conceptList),action(() -> conceptDetails(controller.getConcept(readId("ID de concepto")))),
                action(() -> saveConcept(controller.getConcept(readId("ID de concepto")))));
    }
    private void saveConcept(PaymentConcept current) {
        PaymentConcept value=new PaymentConcept();value.setId(current==null ? null : current.getId());
        value.setCode(required("Codigo",current==null ? null : current.getCode()));value.setName(required("Nombre",current==null ? null : current.getName()));
        value.setDescription(optional("Descripcion",current==null ? null : current.getDescription()));
        value.setBaseAmount(current!=null && !readYesNo("Cambiar valor base ["+current.getBaseAmount()+"]") ? current.getBaseAmount() : amount("Valor base sin IVA"));
        value.setVatRate(current!=null && !readYesNo("Cambiar IVA ["+current.getVatRate()+"]") ? current.getVatRate() : readDecimal("Porcentaje IVA: ",BigDecimal.ZERO,new BigDecimal("100"),2));
        value.setRecurring(readYesNo("Concepto recurrente"));value.setActive(readYesNo("Concepto activo"));conceptDetails(value);
        if(readYesNo("Guardar concepto")) printSuccess("Concepto guardado. ID: "+controller.saveConcept(value));else printInfo("No se guardaron cambios.");
    }
    private void conceptList() {
        printTable(List.of("ID","Codigo","Concepto","Base COP","IVA %","Recurrente","Activo"),controller.findConcepts().stream().map(value -> row(value.getId(),value.getCode(),value.getName(),value.getBaseAmount(),value.getVatRate(),value.isRecurring(),value.isActive())).toList());
    }
    private void conceptDetails(PaymentConcept value) {
        printHeader("Detalle de concepto");field("ID",value.getId());field("Codigo",value.getCode());field("Nombre",value.getName());field("Descripcion",value.getDescription());
        field("Valor base sin IVA",value.getBaseAmount());field("IVA %",value.getVatRate());field("Recurrente",value.isRecurring());field("Activo",value.isActive());
    }
    private void priceMenu() {
        menu("Precios Personalizados","Volver",new String[]{"Registrar precio","Consultar por estudiante","Actualizar precio"},action(() -> savePrice(null)),
                action(() -> showPrices(controller.findPrices(student()))),action(() -> savePrice(controller.getPrice(readId("ID del precio")))));
    }
    private void savePrice(StudentPrice current) {
        StudentPrice value=new StudentPrice();value.setId(current==null ? null : current.getId());value.setStudentId(current==null ? student() : current.getStudentId());value.setConceptId(current==null ? concept() : current.getConceptId());
        value.setAmount(amount("Valor personalizado sin IVA"));value.setReason(required("Motivo",current==null ? null : current.getReason()));
        value.setValidFrom(date("Vigente desde",current==null ? null : current.getValidFrom()));
        if(readYesNo("Definir fecha final de vigencia")) value.setValidUntil(readDate("Vigente hasta (dd/MM/yyyy): "));
        showPrices(List.of(value));printInfo("Los precios se aplican a los nuevos cargos segun el primer dia del mes causado.");
        if(readYesNo("Guardar precio")) printSuccess("Precio guardado. ID: "+controller.savePrice(value));else printInfo("No se guardaron cambios.");
    }
    private void showPrices(List<StudentPrice> values) {
        printTable(List.of("ID","Estudiante ID","Concepto","Valor COP","Desde","Hasta","Motivo"),values.stream().map(value -> row(value.getId(),value.getStudentId(),controller.getConcept(value.getConceptId()).getName(),value.getAmount(),value.getValidFrom(),value.getValidUntil(),value.getReason())).toList());
    }
    private void chargeMenu() {
        menu("Cargos y Mora","Volver",new String[]{"Crear cargo","Cargos de estudiante","Anular cargo pendiente","Morosos con mas de un mes de atraso"},
                action(this::createCharge),action(() -> showCharges(controller.findCharges(student()))),action(this::cancelCharge),action(() -> showCharges(controller.findOverdueCharges(readDate("Fecha de corte (dd/MM/yyyy): ")))));
    }
    private void createCharge() {
        StudentCharge value=new StudentCharge();value.setStudentId(student());value.setConceptId(concept());value.setAcademicYear(readInt("Anio academico: ",1900,9999));
        value.setPeriod(period());value.setMonth(readInt("Mes causado: ",1,12));value.setDueDate(readDate("Fecha de vencimiento (dd/MM/yyyy): "));value.setStatus(ChargeStatus.PENDING);
        value.setAmount(controller.previewChargeAmount(value.getStudentId(),value.getConceptId(),value.getAcademicYear(),value.getMonth()));showCharges(List.of(value));
        if(readYesNo("Crear cargo")) printSuccess("Cargo creado. ID: "+controller.createCharge(value));else printInfo("No se guardaron cambios.");
    }
    private void showCharges(List<StudentCharge> values) {
        printTable(List.of("ID","Estudiante","Concepto","Base COP","Anio","Mes","Periodo","Vence","Estado","Pago ID"),values.stream().map(value -> row(value.getId(),students.getById(value.getStudentId()).getFullName(),controller.getConcept(value.getConceptId()).getName(),value.getAmount(),value.getAcademicYear(),value.getMonth(),value.getPeriod().getDbValue(),value.getDueDate(),value.getStatus().getDbValue(),value.getPaymentId())).toList());
    }
    private void cancelCharge() {
        StudentCharge value=controller.getCharge(readId("ID del cargo"));showCharges(List.of(value));
        if(readYesNo("Anular cargo pendiente")) { controller.cancelCharge(value.getId());printSuccess("Cargo anulado."); } else printInfo("No se guardaron cambios.");
    }
    private void paymentMenu() {
        menu("Pagos e Historial","Volver",new String[]{"Pagar cargos","Historial por estudiante","Historial por acudiente"},action(this::pay),
                action(() -> showPayments(controller.findPaymentsByStudent(student()))),action(() -> showPayments(controller.findPaymentsByGuardian(guardian()))));
    }
    private void pay() {
        int guardianId=guardian();int count=readInt("Cuantos cargos desea pagar: ",1,100);List<Integer> ids=new ArrayList<>();
        for(int i=0;i<count;i++) ids.add(readId("ID del cargo "+(i+1)));
        List<ChargeQuote> quote=controller.quoteCharges(ids,guardianId);
        printTable(List.of("Cargo ID","Estudiante","Concepto","Base COP","IVA COP","Total COP"),quote.stream().map(value -> row(value.charge().getId(),value.studentName(),value.conceptName(),value.charge().getAmount(),value.vat(),value.total())).toList());
        field("Total a pagar COP",quote.stream().map(ChargeQuote::total).reduce(BigDecimal.ZERO,BigDecimal::add));
        PaymentMethod method=choose("Metodo de pago",PaymentMethod.values(),PaymentMethod::getDbValue,null);LocalDate date=readDate("Fecha de pago (dd/MM/yyyy): ");
        if(readYesNo("Confirmar pago")) {
            PaymentOutcome result=controller.payCharges(ids,guardianId,method,date);printSuccess("Pago confirmado.");field("Total COP",result.total());showPayments(result.payments());
        } else printInfo("No se registro el pago.");
    }
    private void showPayments(List<Payment> values) {
        printTable(List.of("ID","Estudiante ID","Acudiente ID","Concepto","Total COP","Fecha","Metodo","Recibo"),values.stream().map(value -> row(value.getId(),value.getStudentId(),value.getGuardianId(),controller.getConcept(value.getConceptId()).getName(),value.getAmount(),value.getDate(),value.getMethod().getDbValue(),value.getReceiptNumber())).toList());
    }
}
