package service;

import dao.*;
import dao.impl.*;
import java.math.*;
import java.sql.Connection;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.report.OverdueChargeRow;
import model.enums.*;
import util.*;
import util.exception.*;
import static service.ServiceValidation.*;

public class FinanceService {
    private static final BigDecimal MAX_MONEY=new BigDecimal("99999999.99");
    private final FinanceDAO finance;
    private final TransactionManager transactions;
    public FinanceService() { this(new FinanceDAOImpl(),ConnectionDB::getConnection); }
    public FinanceService(FinanceDAO finance,Supplier<Connection> connections) {
        this.finance=Objects.requireNonNull(finance);
        this.transactions=new TransactionManager(connections);
    }
    private void money(BigDecimal value,String field) {
        require(value!=null && value.signum()>=0 && value.compareTo(MAX_MONEY)<=0 && value.stripTrailingZeros().scale()<=2,field+" debe estar entre 0 y 99999999.99, con maximo dos decimales.");
    }
    private Student student(Integer id,Connection c) { ServiceValidation.id(id);return finance.findStudent(id,c).orElseThrow(() -> new ValidationException("Estudiante no encontrado.")); }
    private Guardian guardian(Integer id,Connection c) { ServiceValidation.id(id);return finance.findGuardian(id,c).orElseThrow(() -> new ValidationException("Acudiente no encontrado.")); }
    private PaymentConcept concept(Integer id,Connection c) { ServiceValidation.id(id);return finance.findPaymentConcept(id,c).orElseThrow(() -> new ValidationException("Concepto no encontrado.")); }
    private StudentCharge charge(Integer id,Connection c) { ServiceValidation.id(id);return finance.findStudentCharge(id,c).orElseThrow(() -> new ValidationException("Cargo no encontrado.")); }
    public PaymentConcept getConcept(Integer id) { return transactions.execute(c -> concept(id,c)); }
    public StudentCharge getCharge(Integer id) { return transactions.execute(c -> charge(id,c)); }
    public List<PaymentConcept> findConcepts() { return transactions.execute(finance::findConcepts); }
    public int saveConcept(PaymentConcept value) {
        require(value!=null,"El concepto es obligatorio.");value.setCode(text(value.getCode(),"El codigo").toUpperCase(Locale.ROOT));value.setName(text(value.getName(),"El nombre"));
        require(value.getCode().length()<=20 && value.getName().length()<=100,"Codigo: maximo 20 caracteres; nombre: maximo 100.");money(value.getBaseAmount(),"El valor base");
        require(value.getVatRate()!=null && value.getVatRate().signum()>=0 && value.getVatRate().compareTo(new BigDecimal("100"))<=0 && value.getVatRate().stripTrailingZeros().scale()<=2,"El IVA debe estar entre 0 y 100, con maximo dos decimales.");
        int id=transactions.execute(c -> {
            finance.findConceptByCode(value.getCode(),c).ifPresent(existing -> require(value.getId()!=null && Objects.equals(existing.getId(),value.getId()),"El codigo pertenece a otro concepto."));
            if(value.getId()==null) return finance.createPaymentConcept(value,c);
            PaymentConcept existing=concept(value.getId(),c);
            require(existing.getVatRate().compareTo(value.getVatRate())==0 || !finance.hasCharges(value.getId(),c),"No se puede cambiar el IVA de un concepto con cargos. Cree otro concepto.");
            finance.updatePaymentConcept(value,c);return value.getId();
        });value.setId(id);return id;
    }
    public StudentPrice getPrice(Integer id) {
        ServiceValidation.id(id);return transactions.execute(c -> finance.findStudentPrice(id,c).orElseThrow(() -> new ValidationException("Precio no encontrado.")));
    }
    public List<StudentPrice> findPrices(Integer studentId) { id(studentId);return transactions.execute(c -> finance.findPrices(studentId,c)); }
    public int savePrice(StudentPrice value) {
        require(value!=null,"El precio es obligatorio.");id(value.getStudentId());id(value.getConceptId());money(value.getAmount(),"El precio");
        value.setReason(text(value.getReason(),"El motivo"));require(value.getReason().length()<=150,"El motivo admite hasta 150 caracteres.");
        require(value.getValidFrom()!=null && (value.getValidUntil()==null || !value.getValidUntil().isBefore(value.getValidFrom())),"La vigencia no es valida.");
        int id=transactions.execute(c -> {
            require(student(value.getStudentId(),c).getStatus()==StudentStatus.ACTIVE,"El estudiante debe estar activo.");
            require(concept(value.getConceptId(),c).isActive(),"El concepto esta inactivo.");
            if(value.getId()!=null) {
                StudentPrice existing=finance.findStudentPrice(value.getId(),c).orElseThrow(() -> new ValidationException("Precio no encontrado."));
                require(Objects.equals(existing.getStudentId(),value.getStudentId()) && Objects.equals(existing.getConceptId(),value.getConceptId()),"No se puede cambiar estudiante o concepto del precio.");
            }
            require(!finance.hasPriceOverlap(value,c),"La vigencia se cruza con otro precio personalizado.");
            if(value.getId()==null) return finance.createStudentPrice(value,c);
            finance.updateStudentPrice(value,c);return value.getId();
        });value.setId(id);return id;
    }
    public BigDecimal previewChargeAmount(Integer studentId,Integer conceptId,int year,int month) {
        id(studentId);ServiceValidation.year(year);require(month>=1 && month<=12,"El mes debe estar entre 1 y 12.");
        return transactions.execute(c -> {
            student(studentId,c);PaymentConcept concept=concept(conceptId,c);
            return finance.findEffectivePrice(studentId,conceptId,LocalDate.of(year,month,1),c).map(StudentPrice::getAmount).orElse(concept.getBaseAmount());
        });
    }
    public int createCharge(StudentCharge value) {
        require(value!=null && value.getId()==null,"El nuevo cargo no debe tener identificador.");id(value.getStudentId());id(value.getConceptId());year(value.getAcademicYear());
        require(value.getMonth()>=1 && value.getMonth()<=12,"El mes debe estar entre 1 y 12.");require(value.getPeriod()!=null,"El periodo es obligatorio.");require(value.getDueDate()!=null,"La fecha de vencimiento es obligatoria.");
        require(value.getPaymentId()==null && (value.getStatus()==null || value.getStatus()==ChargeStatus.PENDING),"El nuevo cargo debe estar pendiente y sin pago.");
        BigDecimal[] amount={null};
        int id=transactions.execute(c -> {
            Student student=student(value.getStudentId(),c);require(student.getStatus()==StudentStatus.ACTIVE,"Solo se pueden crear cargos para estudiantes activos.");
            PaymentConcept concept=concept(value.getConceptId(),c);require(concept.isActive(),"El concepto esta inactivo.");
            LocalDate priceDate=LocalDate.of(value.getAcademicYear(),value.getMonth(),1);
            require(!value.getDueDate().isBefore(student.getAdmissionDate()),"El vencimiento no puede ser anterior al ingreso al colegio.");
            require(finance.findChargeByKey(student.getId(),concept.getId(),value.getAcademicYear(),value.getMonth(),c).isEmpty(),"Ya existe un cargo para ese estudiante, concepto, anio y mes.");
            amount[0]=finance.findEffectivePrice(student.getId(),concept.getId(),priceDate,c).map(StudentPrice::getAmount).orElse(concept.getBaseAmount()).setScale(2);
            StudentCharge pending=new StudentCharge();pending.setStudentId(student.getId());pending.setConceptId(concept.getId());pending.setAmount(amount[0]);pending.setAcademicYear(value.getAcademicYear());
            pending.setPeriod(value.getPeriod());pending.setMonth(value.getMonth());pending.setDueDate(value.getDueDate());pending.setStatus(ChargeStatus.PENDING);
            return finance.createStudentCharge(pending,c);
        });value.setId(id);value.setAmount(amount[0]);value.setStatus(ChargeStatus.PENDING);return id;
    }
    public void cancelCharge(Integer id) {
        transactions.execute(c -> { StudentCharge value=charge(id,c);require(value.getStatus()==ChargeStatus.PENDING,"Solo se pueden anular cargos pendientes.");finance.cancelCharge(id,c);return null; });
    }
    public List<StudentCharge> findCharges(Integer studentId) { id(studentId);return transactions.execute(c -> finance.findCharges(studentId,c)); }
    public List<StudentCharge> findOverdueCharges(LocalDate asOf) {
        require(asOf!=null,"La fecha de corte es obligatoria.");return transactions.execute(c -> finance.findOverdueCharges(asOf.minusMonths(1),c));
    }
    public Path exportOverduePortfolio(LocalDate cutoff) {
        require(cutoff != null && !cutoff.isAfter(LocalDate.now()),
                "La fecha de corte es obligatoria y no puede ser futura.");
        List<OverdueChargeRow> charges = transactions.execute(
                c -> finance.findOverduePortfolio(cutoff, cutoff.minusMonths(1), c));
        return OverduePortfolioTxt.write(Path.of("reportes"), cutoff, charges);
    }

    private List<ChargeQuote> quote(List<Integer> chargeIds,Integer guardianId,Connection c) {
        require(chargeIds!=null && !chargeIds.isEmpty(),"Seleccione al menos un cargo.");require(chargeIds.size()<=100,"Puede pagar hasta 100 cargos por operacion.");id(guardianId);
        for(Integer id:chargeIds) ServiceValidation.id(id);
        require(new HashSet<>(chargeIds).size()==chargeIds.size(),"No repita cargos en el mismo pago.");guardian(guardianId,c);
        List<ChargeQuote> values=new ArrayList<>();
        // Stable ordering prevents two payment requests from locking the same charges in reverse order.
        for(Integer id:chargeIds.stream().sorted().toList()) {
            StudentCharge charge=charge(id,c);require(charge.getStatus()==ChargeStatus.PENDING,"El cargo "+id+" no esta pendiente.");
            require(charge.getAmount().signum()>0,"Los cargos de valor cero no requieren pago; pueden anularse.");
            Student student=student(charge.getStudentId(),c);require(finance.isFinancialResponsible(student.getId(),guardianId,c),"El acudiente no es responsable financiero del estudiante "+student.getId()+".");
            PaymentConcept concept=concept(charge.getConceptId(),c);
            BigDecimal vat=charge.getAmount().multiply(concept.getVatRate()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
            BigDecimal total=charge.getAmount().add(vat);money(total,"El total del cargo");
            values.add(new ChargeQuote(charge,student.getFullName(),concept.getName(),concept.getVatRate(),vat,total));
        }
        return values;
    }
    public List<ChargeQuote> quoteCharges(List<Integer> ids,Integer guardianId) { return transactions.execute(c -> quote(ids,guardianId,c)); }
    public PaymentOutcome payCharges(List<Integer> ids,Integer guardianId,PaymentMethod method,LocalDate date) {
        require(method!=null,"El metodo de pago es obligatorio.");
        require(date!=null && !date.isAfter(LocalDate.now()),"La fecha de pago no puede ser futura.");
        return transactions.execute(c -> {
            List<ChargeQuote> quoted=quote(ids,guardianId,c);
            List<Payment> paid=new ArrayList<>();
            for(ChargeQuote value:quoted) {
                StudentCharge charge=value.charge();Student student=student(charge.getStudentId(),c);
                require(!date.isBefore(student.getAdmissionDate()),"El pago no puede ser anterior al ingreso al colegio.");
                Payment payment=new Payment();payment.setStudentId(charge.getStudentId());payment.setGuardianId(guardianId);
                payment.setConceptId(charge.getConceptId());payment.setAmount(value.total());payment.setDate(date);payment.setMethod(method);
                payment.setReceiptNumber("REC-"+UUID.randomUUID());payment.setAcademicYear(charge.getAcademicYear());
                payment.setPeriod(charge.getPeriod());payment.setNotes("Cargo "+charge.getId()+" mes "+charge.getMonth());
                payment.setId(finance.createPayment(payment,c));finance.settleCharge(charge.getId(),payment.getId(),c);paid.add(payment);
            }
            return new PaymentOutcome(paid, quoted);
        });
    }
    public List<Payment> findPaymentsByStudent(Integer id) { ServiceValidation.id(id);return transactions.execute(c -> finance.findPaymentsByStudent(id,c)); }
    public List<Payment> findPaymentsByGuardian(Integer id) { ServiceValidation.id(id);return transactions.execute(c -> finance.findPaymentsByGuardian(id,c)); }
}
