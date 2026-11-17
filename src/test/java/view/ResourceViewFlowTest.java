package view;

import controller.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;
import util.exception.ValidationException;

/** Run each scenario in a fresh JVM to isolate the shared console scanner. */
public class ResourceViewFlowTest {
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    private static ResourceLoan loan(BorrowerType type) {
        ResourceLoan value=new ResourceLoan();value.setId(2);value.setResourceId(1);value.setBorrowerId(1);value.setBorrowerType(type);value.setLoanDate(LocalDate.of(2026,11,1));
        value.setExpectedReturnDate(LocalDate.of(2026,11,10));value.setStatus(LoanStatus.OVERDUE);return value;
    }
    public static void main(String[] args) {
        String scenario=args[0];int[] writes={0};
        ResourceCategory category=new ResourceCategory();category.setId(1);category.setName("Libros");
        Resource resource=new Resource();resource.setId(1);resource.setCode("LIB-1");resource.setName("Libro");resource.setCategoryId(1);resource.setStatus(ResourceStatus.AVAILABLE);
        Student student=new Student();student.setId(1);student.setFirstName("Ana");student.setFirstLastName("Perez");
        Teacher teacher=new Teacher();teacher.setId(1);teacher.setFirstName("Maria");teacher.setFirstLastName("Gil");
        ResourceLoan studentLoan=loan(BorrowerType.STUDENT),teacherLoan=loan(BorrowerType.TEACHER);
        ResourceController resources=new ResourceController() {
            @Override public ResourceCategory getCategory(Integer id) { return category; }
            @Override public List<ResourceCategory> findCategories() { return List.of(category); }
            @Override public int saveCategory(ResourceCategory value) { check(value.getName().equals("Libros"),"Category form");writes[0]++;return 10; }
            @Override public Resource getResource(Integer id) { return resource; }
            @Override public Optional<Resource> findByCode(String code) { return Optional.empty(); }
            @Override public List<Resource> findResources(String query,Integer categoryId,ResourceStatus status) {
                if(scenario.equals("filter")) check(categoryId==1 && status==ResourceStatus.AVAILABLE,"Catalog filters");return List.of(resource);
            }
            @Override public int saveResource(Resource value) { check(value.getCode().equals("LIB-1") && value.getCategoryId()==1 && value.getStatus()==ResourceStatus.AVAILABLE,"Resource form");writes[0]++;return 20; }
            @Override public void changeStatus(Integer id,ResourceStatus status) { check(id==1 && status==ResourceStatus.MAINTENANCE,"Resource state selection");writes[0]++; }
            @Override public int lend(ResourceLoan value) {
                if(scenario.equals("unavailable")) throw new ValidationException("No disponible");
                check(value.getResourceId()==1 && value.getBorrowerId()==1 && value.getLoanDate().equals(LocalDate.of(2026,11,1)),"Loan form");
                check(value.getBorrowerType()==(scenario.equals("lend-teacher") ? BorrowerType.TEACHER : BorrowerType.STUDENT),"Wrong borrower table selected");writes[0]++;return 30;
            }
            @Override public ResourceLoan getLoan(BorrowerType type,Integer id) { return type==BorrowerType.STUDENT ? studentLoan : teacherLoan; }
            @Override public void returnResource(BorrowerType type,Integer id,LocalDate date) {
                check(type==(scenario.equals("return-teacher") ? BorrowerType.TEACHER : BorrowerType.STUDENT) && id==2 && date.equals(LocalDate.of(2026,11,16)),"Return identity or date");writes[0]++;
            }
            @Override public List<ResourceLoan> findLoansByBorrower(BorrowerType type,Integer id) { return List.of(type==BorrowerType.STUDENT ? studentLoan : teacherLoan); }
            @Override public List<ResourceLoan> findOverdueLoans(LocalDate date) { return List.of(studentLoan,teacherLoan); }
        };
        StudentController students=new StudentController() { @Override public Student getById(Integer id) { return student; } };
        TeacherController teachers=new TeacherController() { @Override public Teacher getById(Integer id) { return teacher; } };
        ResourceView view=new ResourceView(resources,students,teachers);
        String studentForm="3\n1\n1\n1\n01/11/2026\n20/11/2026\n\n";
        String input=switch(scenario) {
            case "navigation" -> "6\n1\n5\n2\n10\n3\n7\n4\n8\n";
            case "category" -> "1\n1\nLibros\n\nsi\n\n5\n4\n";
            case "resource" -> "2\n1\nLIB-1\nLibro\n\n1\nno\nBiblioteca\nsi\n\n10\n4\n";
            case "filter" -> "2\n4\nsi\n1\nsi\n1\n\n10\n4\n";
            case "cancel-edit" -> "2\n7\n1\nChanged\ncancelar\n10\n4\n";
            case "state" -> "2\n8\n1\n2\nsi\n\n10\n4\n";
            case "lend-student", "unavailable" -> studentForm+"si\n\n7\n4\n";
            case "decline-loan" -> studentForm+"no\n\n7\n4\n";
            case "lend-teacher" -> "3\n2\n1\n1\n01/11/2026\n20/11/2026\n\nsi\n\n7\n4\n";
            case "return-student" -> "3\n3\n1\n2\n16/11/2026\nsi\n\n7\n4\n";
            case "return-teacher" -> "3\n3\n2\n2\n16/11/2026\nsi\n\n7\n4\n";
            case "cancel-return" -> "3\n3\n1\n2\ncancelar\n7\n4\n";
            case "history" -> "3\n5\n2\n1\n\n7\n4\n";
            case "overdue" -> "3\n6\n16/11/2026\n\n7\n4\n";
            case "missing-code" -> "2\n6\nUNKNOWN\n\n10\n4\n";
            default -> throw new IllegalArgumentException(scenario);
        };
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream output=new ByteArrayOutputStream();PrintStream console=System.out,errors=System.err;
        try(PrintStream capture=new PrintStream(output,true,StandardCharsets.UTF_8)) {
            System.setOut(capture);System.setErr(capture);
            if(scenario.equals("navigation")) new MainMenuView(new StudentView(),new TeacherView(),new AcademicView(),new ExtracurricularView(),new FinanceView(),view).start();else view.start();
        } finally { System.setOut(console);System.setErr(errors); }
        String text=output.toString(StandardCharsets.UTF_8);
        check(writes[0]==(Set.of("category","resource","state","lend-student","lend-teacher","return-student","return-teacher").contains(scenario)?1:0),"Unexpected writes");
        check(resource.getCode().equals("LIB-1") && studentLoan.getStatus()==LoanStatus.OVERDUE,"View mutated source objects");
        if(scenario.startsWith("cancel")) check(text.contains("Operacion cancelada"),"Cancellation missing");
        if(scenario.equals("unavailable")) check(text.contains("No disponible"),"Availability error missing");
        if(scenario.equals("history")) check(text.contains("Maria Gil"),"Teacher history missing");
        if(scenario.equals("overdue")) check(text.contains("Ana Perez") && text.contains("Maria Gil"),"Combined overdue report missing");
        if(scenario.equals("missing-code")) check(text.contains("Recurso no encontrado"),"Missing code message absent");
        if(scenario.equals("navigation")) check(text.contains("Hasta pronto"),"Main menu exit failed");
        System.out.println("Passed resource view scenario: "+scenario);
    }
}
