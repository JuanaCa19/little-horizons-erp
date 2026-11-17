package service;

import dao.*;
import dao.impl.*;
import java.sql.Connection;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import model.*;
import model.enums.*;
import util.ConnectionDB;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class ResourceService {
    private final ResourceDAO resources;
    private final LoanDAO loans;
    private final TransactionManager transactions;
    private final Clock clock;
    public ResourceService() { this(new ResourceDAOImpl(),new LoanDAOImpl(),ConnectionDB::getConnection); }
    public ResourceService(ResourceDAO resources,LoanDAO loans,Supplier<Connection> connections) { this(resources,loans,connections,Clock.systemDefaultZone()); }
    public ResourceService(ResourceDAO resources,LoanDAO loans,Supplier<Connection> connections,Clock clock) {
        this.resources=Objects.requireNonNull(resources);this.loans=Objects.requireNonNull(loans);
        this.transactions=new TransactionManager(connections);this.clock=Objects.requireNonNull(clock);
    }
    private LocalDate today() { return LocalDate.now(clock); }
    private ResourceCategory category(Integer id,Connection c) {
        ServiceValidation.id(id);return resources.findCategory(id,c).orElseThrow(() -> new ValidationException("Categoria no encontrada."));
    }
    private Resource resource(Integer id,Connection c) {
        ServiceValidation.id(id);return resources.findById(id,c).orElseThrow(() -> new ValidationException("Recurso no encontrado."));
    }
    private void borrower(BorrowerType type,Integer borrowerId) { require(type!=null,"El tipo de solicitante es obligatorio.");id(borrowerId); }
    public int saveCategory(ResourceCategory value) {
        require(value!=null,"La categoria es obligatoria.");value.setName(text(value.getName(),"El nombre"));
        require(value.getName().length()<=50,"El nombre de categoria admite hasta 50 caracteres.");
        int id=transactions.execute(c -> {
            resources.findCategoryByName(value.getName(),c).ifPresent(existing -> require(value.getId()!=null && Objects.equals(value.getId(),existing.getId()),"Ya existe una categoria con ese nombre."));
            if(value.getId()==null) return resources.createCategory(value,c);
            category(value.getId(),c);resources.updateCategory(value,c);return value.getId();
        });value.setId(id);return id;
    }
    public ResourceCategory getCategory(Integer id) { return transactions.execute(c -> category(id,c)); }
    public List<ResourceCategory> findCategories() { return transactions.execute(resources::findCategories); }
    private void validate(Resource value) {
        require(value!=null,"El recurso es obligatorio.");value.setCode(text(value.getCode(),"El codigo").toUpperCase(Locale.ROOT));value.setName(text(value.getName(),"El nombre"));
        require(value.getCode().length()<=50 && value.getName().length()<=150,"Codigo: maximo 50 caracteres; nombre: maximo 150.");
        id(value.getCategoryId());require(value.getStatus()!=null,"El estado del recurso es obligatorio.");
        require(value.getLocation()==null || value.getLocation().length()<=100,"La ubicacion admite hasta 100 caracteres.");
        require(value.getAcquisitionDate()==null || !value.getAcquisitionDate().isAfter(today()),"La fecha de adquisicion no puede ser futura.");
    }
    public int saveResource(Resource value) {
        validate(value);
        int id=transactions.execute(c -> {
            Resource existing=value.getId()==null ? null : resource(value.getId(),c);
            category(value.getCategoryId(),c);
            resources.findByCode(value.getCode(),c).ifPresent(other -> require(value.getId()!=null && Objects.equals(value.getId(),other.getId()),"El codigo pertenece a otro recurso."));
            if(existing==null) {
                require(value.getStatus()!=ResourceStatus.LOANED,"Un recurso nuevo no puede estar prestado.");return resources.create(value,c);
            }
            require(existing.getStatus()==value.getStatus(),"Use la operacion de cambio de estado.");
            if(value.getAcquisitionDate()!=null) require(loans.findByResource(value.getId(),c).stream().noneMatch(loan -> loan.getLoanDate().isBefore(value.getAcquisitionDate())),
                    "La fecha de adquisicion no puede ser posterior al historial de prestamos.");
            resources.update(value,c);return value.getId();
        });value.setId(id);return id;
    }
    public Resource getResource(Integer id) { return transactions.execute(c -> resource(id,c)); }
    public Optional<Resource> findByCode(String code) { String normalized=text(code,"El codigo");return transactions.execute(c -> resources.findByCode(normalized,c)); }
    public List<Resource> findResources(String query,Integer categoryId,ResourceStatus status) {
        String search=query==null || query.isBlank() ? null : query.trim();
        if(categoryId!=null) id(categoryId);
        return transactions.execute(c -> { if(categoryId!=null) category(categoryId,c);return resources.findResources(search,categoryId,status,c); });
    }
    public void changeStatus(Integer resourceId,ResourceStatus status) {
        require(status!=null && status!=ResourceStatus.LOANED,"El estado Prestado se asigna mediante un prestamo.");
        transactions.execute(c -> {
            Resource value=resource(resourceId,c);require(!loans.hasOpenLoan(resourceId,c),"El recurso tiene un prestamo abierto; registre primero su devolucion.");
            if(value.getStatus()!=status) resources.updateStatus(resourceId,status,c);return null;
        });
    }
    public int lend(ResourceLoan value) {
        require(value!=null && value.getId()==null,"El nuevo prestamo no debe tener identificador.");id(value.getResourceId());borrower(value.getBorrowerType(),value.getBorrowerId());
        require(value.getLoanDate()!=null && !value.getLoanDate().isAfter(today()),"La fecha de prestamo no puede ser futura.");
        require(value.getExpectedReturnDate()!=null && !value.getExpectedReturnDate().isBefore(value.getLoanDate()),"La devolucion programada no puede ser anterior al prestamo.");
        require(value.getActualReturnDate()==null && value.getStatus()!=LoanStatus.RETURNED,"Un nuevo prestamo no puede estar devuelto.");
        LoanStatus status=value.getExpectedReturnDate().isBefore(today()) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
        require(value.getStatus()==null || value.getStatus()==status,"El estado del prestamo debe corresponder a su fecha de vencimiento.");
        int id=transactions.execute(c -> {
            // The shared resource row serializes lending across both borrower tables.
            Resource resource=resource(value.getResourceId(),c);
            require(resource.getStatus()==ResourceStatus.AVAILABLE,"El recurso no esta disponible.");
            require(!loans.hasOpenLoan(resource.getId(),c),"El recurso ya tiene un prestamo abierto.");
            LocalDate admission=loans.requireActiveBorrower(value.getBorrowerType(),value.getBorrowerId(),c);
            require(!value.getLoanDate().isBefore(admission),"El prestamo no puede ser anterior al ingreso o contratacion del solicitante.");
            require(resource.getAcquisitionDate()==null || !value.getLoanDate().isBefore(resource.getAcquisitionDate()),"El prestamo no puede ser anterior a la adquisicion del recurso.");
            loans.latestReturnDate(resource.getId(),c).ifPresent(last -> require(!value.getLoanDate().isBefore(last),"El nuevo prestamo no puede ser anterior a la ultima devolucion."));
            ResourceLoan pending=new ResourceLoan();pending.setResourceId(value.getResourceId());pending.setBorrowerType(value.getBorrowerType());pending.setBorrowerId(value.getBorrowerId());
            pending.setLoanDate(value.getLoanDate());pending.setExpectedReturnDate(value.getExpectedReturnDate());pending.setStatus(status);pending.setNotes(value.getNotes());
            int generated=loans.create(pending,c);resources.updateStatus(resource.getId(),ResourceStatus.LOANED,c);return generated;
        });value.setId(id);value.setStatus(status);return id;
    }
    private ResourceLoan loan(BorrowerType type,Integer id,Connection c) {
        require(type!=null,"El tipo de solicitante es obligatorio.");ServiceValidation.id(id);
        return loans.findById(type,id,c).orElseThrow(() -> new ValidationException("Prestamo no encontrado para ese tipo de solicitante."));
    }
    public ResourceLoan getLoan(BorrowerType type,Integer id) { return transactions.execute(c -> effective(loan(type,id,c),today())); }
    private ResourceLoan effective(ResourceLoan value,LocalDate date) { value.setStatus(value.effectiveStatus(date));return value; }
    public void returnResource(BorrowerType type,Integer loanId,LocalDate date) {
        require(date!=null && !date.isAfter(today()),"La fecha de devolucion no puede ser futura.");ResourceLoan snapshot=getLoan(type,loanId);
        transactions.execute(c -> {
            resource(snapshot.getResourceId(),c);ResourceLoan current=loan(type,loanId,c);
            require(current.getStatus()!=LoanStatus.RETURNED,"El prestamo ya fue devuelto.");require(!date.isBefore(current.getLoanDate()),"La devolucion no puede ser anterior al prestamo.");
            loans.markReturned(type,loanId,date,c);
            resources.updateStatus(current.getResourceId(),loans.hasOpenLoan(current.getResourceId(),c) ? ResourceStatus.LOANED : ResourceStatus.AVAILABLE,c);
            return null;
        });
    }
    public List<ResourceLoan> findLoansByResource(Integer resourceId) {
        return transactions.execute(c -> { resource(resourceId,c);return loans.findByResource(resourceId,c).stream().map(value -> effective(value,today())).toList(); });
    }
    public List<ResourceLoan> findLoansByBorrower(BorrowerType type,Integer borrowerId) {
        borrower(type,borrowerId);return transactions.execute(c -> loans.findByBorrower(type,borrowerId,c).stream().map(value -> effective(value,today())).toList());
    }
    public List<ResourceLoan> findOverdueLoans(LocalDate date) {
        require(date!=null,"La fecha de corte es obligatoria.");return transactions.execute(c -> loans.findOverdue(date,c).stream().map(value -> effective(value,date)).toList());
    }
}
