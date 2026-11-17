package view;

import controller.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.CancelledException;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class ResourceView {
    private final ResourceController controller;
    private final StudentController students;
    private final TeacherController teachers;
    public ResourceView() { this(new ResourceController(),new StudentController(),new TeacherController()); }
    public ResourceView(ResourceController controller,StudentController students,TeacherController teachers) {
        this.controller=Objects.requireNonNull(controller);this.students=Objects.requireNonNull(students);this.teachers=Objects.requireNonNull(teachers);
    }
    public void start() {
        menu("Gestion de Recursos","Volver al menu principal",new String[]{"Categorias","Catalogo de recursos","Prestamos y devoluciones"},this::categoryMenu,this::catalogMenu,this::loanMenu);
    }
    private void categoryMenu() {
        menu("Categorias de Recursos","Volver",new String[]{"Crear categoria","Listar categorias","Consultar detalle","Actualizar categoria"},
                action(() -> saveCategory(null)),action(this::showCategories),action(() -> categoryDetails(controller.getCategory(readId("ID de categoria")))),
                action(() -> saveCategory(controller.getCategory(readId("ID de categoria")))));
    }
    private void saveCategory(ResourceCategory current) {
        ResourceCategory value=new ResourceCategory();value.setId(current==null ? null : current.getId());
        value.setName(required("Nombre de categoria",current==null ? null : current.getName()));value.setDescription(optional("Descripcion",current==null ? null : current.getDescription()));categoryDetails(value);
        if(readYesNo("Guardar categoria")) printSuccess("Categoria guardada. ID: "+controller.saveCategory(value));else printInfo("No se guardaron cambios.");
    }
    private void categoryDetails(ResourceCategory value) { printHeader("Detalle de categoria");field("ID",value.getId());field("Nombre",value.getName());field("Descripcion",value.getDescription()); }
    private void showCategories() {
        printTable(List.of("ID","Categoria","Descripcion"),controller.findCategories().stream().map(value -> row(value.getId(),value.getName(),value.getDescription())).toList());
    }
    private int category() {
        List<ResourceCategory> categories=controller.findCategories();
        printTable(List.of("ID","Categoria"),categories.stream().map(value -> row(value.getId(),value.getName())).toList());
        if(categories.isEmpty()) { printInfo("Registre primero una categoria.");throw new CancelledException(); }
        while(true) {
            int id=readId("ID de categoria");if(categories.stream().anyMatch(value -> value.getId()==id)) return id;
            printError("Seleccione una categoria de la lista.");
        }
    }
    private Resource resource() {
        Resource value=controller.getResource(readId("ID del recurso"));field("Recurso",value.getName());return value;
    }
    private void catalogMenu() {
        menu("Catalogo de Recursos","Volver",new String[]{"Registrar recurso","Listar recursos","Buscar por nombre o codigo","Filtrar por categoria o estado","Consultar detalle",
                "Consultar por codigo","Actualizar datos","Cambiar estado","Historial de prestamos del recurso"},
                action(() -> saveResource(null)),action(() -> showResources(controller.findResources(null,null,null))),
                action(() -> showResources(controller.findResources(readRequiredText("Nombre o codigo: "),null,null))),action(this::filter),
                action(() -> resourceDetails(resource())),action(this::findByCode),action(() -> saveResource(resource())),action(this::changeStatus),
                action(() -> showLoans(controller.findLoansByResource(resource().getId()))));
    }
    private void saveResource(Resource current) {
        Resource value=new Resource();value.setId(current==null ? null : current.getId());value.setStatus(current==null ? ResourceStatus.AVAILABLE : current.getStatus());
        if(current!=null) printInfo("Enter conserva los textos actuales. - borra los campos opcionales.");
        value.setCode(required("Codigo de inventario",current==null ? null : current.getCode()));value.setName(required("Nombre del recurso",current==null ? null : current.getName()));
        value.setDescription(optional("Descripcion",current==null ? null : current.getDescription()));
        value.setCategoryId(current!=null && !readYesNo("Cambiar categoria ["+current.getCategoryId()+"]") ? current.getCategoryId() : category());
        if(current!=null && !readYesNo("Cambiar fecha de adquisicion ["+value(current.getAcquisitionDate())+"]")) value.setAcquisitionDate(current.getAcquisitionDate());
        else if(readYesNo("Registrar fecha de adquisicion")) value.setAcquisitionDate(readDate("Adquisicion (dd/MM/yyyy): "));
        value.setLocation(optional("Ubicacion fisica",current==null ? null : current.getLocation()));resourceDetails(value);
        if(readYesNo("Guardar recurso")) printSuccess("Recurso guardado. ID: "+controller.saveResource(value));else printInfo("No se guardaron cambios.");
    }
    private void showResources(List<Resource> values) {
        printTable(List.of("ID","Codigo","Recurso","Categoria","Estado","Ubicacion"),values.stream().map(value -> row(value.getId(),value.getCode(),value.getName(),
                controller.getCategory(value.getCategoryId()).getName(),value.getStatus().getDbValue(),value.getLocation())).toList());
    }
    private void resourceDetails(Resource value) {
        printHeader("Detalle de recurso");field("ID",value.getId());field("Codigo",value.getCode());field("Nombre",value.getName());
        field("Categoria",controller.getCategory(value.getCategoryId()).getName());field("Descripcion",value.getDescription());field("Adquisicion",value.getAcquisitionDate());
        field("Estado",value.getStatus().getDbValue());field("Ubicacion",value.getLocation());
    }
    private void filter() {
        Integer categoryId=readYesNo("Filtrar por categoria") ? category() : null;
        ResourceStatus status=readYesNo("Filtrar por estado") ? choose("Estado",ResourceStatus.values(),ResourceStatus::getDbValue,null) : null;
        showResources(controller.findResources(null,categoryId,status));
    }
    private void findByCode() { controller.findByCode(readRequiredText("Codigo: ")).ifPresentOrElse(this::resourceDetails,() -> printInfo("Recurso no encontrado.")); }
    private void changeStatus() {
        Resource value=resource();resourceDetails(value);
        ResourceStatus[] statuses={ResourceStatus.AVAILABLE,ResourceStatus.MAINTENANCE,ResourceStatus.RETIRED};
        ResourceStatus status=choose("Nuevo estado",statuses,ResourceStatus::getDbValue,null);
        if(readYesNo("Confirmar cambio de estado")) { controller.changeStatus(value.getId(),status);printSuccess("Estado actualizado."); } else printInfo("No se guardaron cambios.");
    }
    private void loanMenu() {
        menu("Prestamos y Devoluciones","Volver",new String[]{"Prestar a estudiante","Prestar a profesor","Registrar devolucion","Consultar prestamo","Historial del solicitante","Prestamos abiertos vencidos"},
                action(() -> lend(BorrowerType.STUDENT)),action(() -> lend(BorrowerType.TEACHER)),action(this::returnResource),action(() -> showLoans(List.of(selectLoan()))),
                action(this::borrowerHistory),action(() -> showLoans(controller.findOverdueLoans(readDate("Fecha de corte (dd/MM/yyyy): ")))));
    }
    private BorrowerType borrowerType() { return choose("Tipo de solicitante",BorrowerType.values(),BorrowerType::getDbValue,null); }
    private int borrower(BorrowerType type) {
        int id=readId("ID del "+type.getDbValue().toLowerCase());field("Solicitante",borrowerName(type,id));return id;
    }
    private String borrowerName(BorrowerType type,Integer id) { return type==BorrowerType.STUDENT ? students.getById(id).getFullName() : teachers.getById(id).getFullName(); }
    private void lend(BorrowerType type) {
        Resource value=resource();resourceDetails(value);ResourceLoan loan=new ResourceLoan();loan.setResourceId(value.getId());loan.setBorrowerType(type);loan.setBorrowerId(borrower(type));
        loan.setLoanDate(readDate("Fecha de prestamo (dd/MM/yyyy): "));loan.setExpectedReturnDate(readDate("Devolucion programada (dd/MM/yyyy): "));
        loan.setNotes(readOptionalText("Observaciones (opcional): "));
        field("Prestamo",loan.getLoanDate());field("Devolucion programada",loan.getExpectedReturnDate());field("Observaciones",loan.getNotes());
        if(readYesNo("Confirmar prestamo")) printSuccess("Prestamo registrado. Tipo: "+type.getDbValue()+". ID: "+controller.lend(loan));else printInfo("No se guardaron cambios.");
    }
    private ResourceLoan selectLoan() {
        BorrowerType type=borrowerType();return controller.getLoan(type,readId("ID del prestamo"));
    }
    private void returnResource() {
        ResourceLoan loan=selectLoan();showLoans(List.of(loan));LocalDate date=readDate("Fecha real de devolucion (dd/MM/yyyy): ");
        if(readYesNo("Confirmar devolucion")) { controller.returnResource(loan.getBorrowerType(),loan.getId(),date);printSuccess("Devolucion registrada y disponibilidad actualizada."); } else printInfo("No se guardaron cambios.");
    }
    private void borrowerHistory() { BorrowerType type=borrowerType();showLoans(controller.findLoansByBorrower(type,borrower(type))); }
    private void showLoans(List<ResourceLoan> values) {
        printTable(List.of("ID","Tipo","Solicitante","Recurso","Prestamo","Programada","Devuelto","Estado","Observaciones"),values.stream().map(value -> row(value.getId(),
                value.getBorrowerType().getDbValue(),borrowerName(value.getBorrowerType(),value.getBorrowerId()),controller.getResource(value.getResourceId()).getName(),value.getLoanDate(),
                value.getExpectedReturnDate(),value.getActualReturnDate(),value.getStatus().getDbValue(),value.getNotes())).toList());
    }
}
