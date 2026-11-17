package integration;

import dao.*;
import dao.impl.*;
import java.lang.reflect.*;
import java.sql.Connection;
import java.time.*;
import java.util.*;
import model.*;
import model.enums.*;
import service.ResourceService;
import util.exception.*;

public class ResourceModuleIntegrationTest {
    private static int checks;
    private static final LocalDate TODAY=LocalDate.of(2026,11,16);
    private static final Clock CLOCK=Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(),ZoneOffset.UTC);
    private static void check(boolean value,String message) { if(!value) throw new AssertionError(message);checks++; }
    private static void rejected(Runnable action) { failure(ValidationException.class,action); }
    private static void failure(Class<? extends Throwable> type,Runnable action) {
        try { action.run(); } catch(Throwable error) { check(type.isInstance(error),"Unexpected exception: "+error);return; }
        throw new AssertionError("Expected "+type.getSimpleName());
    }
    private static Resource resource(String code,int category) {
        Resource value=new Resource();value.setCode(code);value.setName("Libro "+code.trim());value.setCategoryId(category);value.setLocation("Biblioteca");value.setAcquisitionDate(LocalDate.of(2024,1,1));value.setStatus(ResourceStatus.AVAILABLE);return value;
    }
    private static ResourceLoan loan(int resource,BorrowerType type,int borrower,LocalDate date,LocalDate due) {
        ResourceLoan value=new ResourceLoan();value.setResourceId(resource);value.setBorrowerType(type);value.setBorrowerId(borrower);value.setLoanDate(date);value.setExpectedReturnDate(due);return value;
    }
    public static void main(String[] args) throws Exception {
        try(TemporaryDatabase db=new TemporaryDatabase("categorias_recurso","recursos","estudiantes","profesores","prestamos_estudiante","prestamos_profesor")) {
            db.execute("ALTER TABLE prestamos_estudiante AUTO_INCREMENT=1");db.execute("ALTER TABLE prestamos_profesor AUTO_INCREMENT=1");
            db.execute("INSERT INTO estudiantes(id_estudiante,tipo_documento,numero_documento,primer_nombre,primer_apellido,fecha_nacimiento,genero,fecha_ingreso,estado) VALUES(1,'TI','111','Ana','Perez','2010-01-01','F','2020-01-01','Activo'),(2,'TI','222','Luis','Ruiz','2010-01-01','M','2020-01-01','Retirado')");
            db.execute("INSERT INTO profesores(id_profesor,tipo_documento,numero_documento,primer_nombre,primer_apellido,correo_electronico,fecha_contratacion,estado) VALUES(1,'CC','100','Maria','Gil','maria@example.com','2020-01-01','Activo'),(2,'CC','200','Jose','Diaz','jose@example.com','2020-01-01','Licencia')");
            ResourceDAO resources=new ResourceDAOImpl();LoanDAO loans=new LoanDAOImpl();ResourceService service=new ResourceService(resources,loans,db.connections(),CLOCK);
            ResourceCategory category=new ResourceCategory();category.setName(" Libros ");category.setDescription("Biblioteca");int categoryId=service.saveCategory(category);
            check(service.getCategory(categoryId).getName().equals("Libros") && service.findCategories().size()==1,"Category normalization and mapping");
            ResourceCategory duplicate=new ResourceCategory();duplicate.setName("LIBROS");rejected(() -> service.saveCategory(duplicate));
            category.setDescription("Libros escolares");service.saveCategory(category);check(service.getCategory(categoryId).getDescription().equals("Libros escolares"),"Category update");
            Resource book=resource(" lib-1 ",categoryId);int bookId=service.saveResource(book);
            Resource second=resource("LIB-2",categoryId);int secondId=service.saveResource(second);
            Resource third=resource("LIB-3",categoryId);int thirdId=service.saveResource(third);
            check(book.getCode().equals("LIB-1") && service.getResource(bookId).getLocation().equals("Biblioteca"),"Inventory normalization and mapping");
            rejected(() -> service.saveResource(resource("LIB-1",categoryId)));
            Resource invalid=resource("BAD",99999);rejected(() -> service.saveResource(invalid));invalid.setCategoryId(categoryId);invalid.setStatus(ResourceStatus.LOANED);rejected(() -> service.saveResource(invalid));
            invalid.setStatus(ResourceStatus.AVAILABLE);invalid.setAcquisitionDate(TODAY.plusDays(1));rejected(() -> service.saveResource(invalid));
            check(service.findByCode("lib-1").isPresent(),"Lookup respects case-insensitive unique code");
            check(service.findResources("Libro",categoryId,ResourceStatus.AVAILABLE).size()==3,"Combined category status and name filters");
            service.changeStatus(thirdId,ResourceStatus.MAINTENANCE);rejected(() -> service.lend(loan(thirdId,BorrowerType.STUDENT,1,TODAY,TODAY.plusDays(2))));
            service.changeStatus(thirdId,ResourceStatus.RETIRED);rejected(() -> service.lend(loan(thirdId,BorrowerType.TEACHER,1,TODAY,TODAY.plusDays(2))));
            check(service.findResources(null,null,ResourceStatus.RETIRED).size()==1,"Retired resources remain in inventory");service.changeStatus(thirdId,ResourceStatus.AVAILABLE);
            rejected(() -> service.changeStatus(bookId,ResourceStatus.LOANED));
            rejected(() -> service.lend(loan(bookId,BorrowerType.STUDENT,2,TODAY,TODAY.plusDays(2))));
            rejected(() -> service.lend(loan(bookId,BorrowerType.TEACHER,2,TODAY,TODAY.plusDays(2))));
            rejected(() -> service.lend(loan(bookId,BorrowerType.STUDENT,99999,TODAY,TODAY.plusDays(2))));
            rejected(() -> service.lend(loan(bookId,BorrowerType.STUDENT,1,TODAY.plusDays(1),TODAY.plusDays(2))));
            rejected(() -> service.lend(loan(bookId,BorrowerType.STUDENT,1,TODAY,TODAY.minusDays(1))));
            rejected(() -> service.lend(loan(bookId,BorrowerType.STUDENT,1,LocalDate.of(2019,1,1),TODAY)));
            rejected(() -> service.lend(loan(bookId,BorrowerType.STUDENT,1,LocalDate.of(2023,1,1),TODAY)));
            ResourceLoan studentLoan=loan(bookId,BorrowerType.STUDENT,1,TODAY.minusDays(10),TODAY.minusDays(1));int studentLoanId=service.lend(studentLoan);
            check(studentLoan.getStatus()==LoanStatus.OVERDUE && service.getLoan(BorrowerType.STUDENT,studentLoanId).getResourceId()==bookId,"Overdue loan mapping");
            check(service.getResource(bookId).getStatus()==ResourceStatus.LOANED,"Lending updates inventory atomically");
            rejected(() -> service.lend(loan(bookId,BorrowerType.TEACHER,1,TODAY,TODAY.plusDays(2))));
            rejected(() -> service.changeStatus(bookId,ResourceStatus.MAINTENANCE));
            check(service.findOverdueLoans(TODAY).size()==1 && service.findOverdueLoans(TODAY.minusDays(1)).isEmpty(),"Overdue cutoff is strictly before reference date");
            // Check both borrower tables even if imported inventory availability is inconsistent.
            db.execute("UPDATE recursos SET estado_recurso='Disponible' WHERE id_recurso="+bookId);
            rejected(() -> service.lend(loan(bookId,BorrowerType.TEACHER,1,TODAY,TODAY.plusDays(1))));
            check(service.findLoansByResource(bookId).size()==1,"No cross-table double loan when inventory is inconsistent");
            db.execute("UPDATE recursos SET estado_recurso='Prestado' WHERE id_recurso="+bookId);
            Resource editing=service.getResource(bookId);editing.setLocation("Biblioteca piso 2");service.saveResource(editing);
            check(service.getResource(bookId).getStatus()==ResourceStatus.LOANED,"Metadata updates preserve loan state");
            editing.setAcquisitionDate(TODAY);rejected(() -> service.saveResource(editing));
            rejected(() -> service.returnResource(BorrowerType.STUDENT,studentLoanId,TODAY.minusDays(11)));
            rejected(() -> service.returnResource(BorrowerType.STUDENT,studentLoanId,TODAY.plusDays(1)));
            db.execute("UPDATE estudiantes SET estado='Retirado' WHERE id_estudiante=1");
            service.returnResource(BorrowerType.STUDENT,studentLoanId,TODAY);
            check(service.getLoan(BorrowerType.STUDENT,studentLoanId).getActualReturnDate().equals(TODAY) && service.getLoan(BorrowerType.STUDENT,studentLoanId).getStatus()==LoanStatus.RETURNED,"Inactive borrower may return a late loan");
            check(service.getResource(bookId).getStatus()==ResourceStatus.AVAILABLE && service.findOverdueLoans(TODAY).isEmpty(),"Return releases inventory and removes overdue alert");
            rejected(() -> service.returnResource(BorrowerType.STUDENT,studentLoanId,TODAY));
            rejected(() -> service.lend(loan(bookId,BorrowerType.TEACHER,1,TODAY.minusDays(1),TODAY.plusDays(1))));
            ResourceLoan teacherLoan=loan(bookId,BorrowerType.TEACHER,1,TODAY,TODAY);int teacherLoanId=service.lend(teacherLoan);
            check(studentLoanId==teacherLoanId && service.getLoan(BorrowerType.STUDENT,studentLoanId).getStatus()==LoanStatus.RETURNED
                    && service.getLoan(BorrowerType.TEACHER,teacherLoanId).getStatus()==LoanStatus.ACTIVE,"Borrower type disambiguates equal loan IDs");
            check(service.findLoansByResource(bookId).size()==2 && service.findLoansByBorrower(BorrowerType.TEACHER,1).size()==1,"Combined resource and borrower histories");
            check(service.findOverdueLoans(TODAY).isEmpty(),"A loan due today is not overdue");
            ResourceService tomorrow=new ResourceService(resources,loans,db.connections(),Clock.offset(CLOCK,Duration.ofDays(1)));
            check(tomorrow.getLoan(BorrowerType.TEACHER,teacherLoanId).getStatus()==LoanStatus.OVERDUE,"Overdue status advances without manual updates");
            ResourceDAO failing=(ResourceDAO)Proxy.newProxyInstance(ResourceDAO.class.getClassLoader(),new Class<?>[]{ResourceDAO.class},(proxy,method,values) -> {
                if(method.getName().equals("updateStatus")) throw new DataAccessException("Simulated availability failure");
                try { return method.invoke(resources,values); } catch(InvocationTargetException e) { throw e.getCause(); }
            });
            ResourceService broken=new ResourceService(failing,loans,db.connections(),CLOCK);
            failure(DataAccessException.class,() -> broken.returnResource(BorrowerType.TEACHER,teacherLoanId,TODAY));
            check(service.getLoan(BorrowerType.TEACHER,teacherLoanId).getStatus()==LoanStatus.ACTIVE && service.getLoan(BorrowerType.TEACHER,teacherLoanId).getActualReturnDate()==null
                    && service.getResource(bookId).getStatus()==ResourceStatus.LOANED,"Failed availability update rolls back loan return");
            ResourceLoan failed=loan(secondId,BorrowerType.TEACHER,1,TODAY,TODAY.plusDays(1));
            failure(DataAccessException.class,() -> broken.lend(failed));
            check(failed.getId()==null && service.findLoansByResource(secondId).isEmpty() && service.getResource(secondId).getStatus()==ResourceStatus.AVAILABLE,"Failed inventory update rolls back new loan and generated id");
            service.returnResource(BorrowerType.TEACHER,teacherLoanId,TODAY);
            db.execute("UPDATE estudiantes SET estado='Activo' WHERE id_estudiante=1");
            service.lend(loan(secondId,BorrowerType.STUDENT,1,TODAY.minusDays(4),TODAY.minusDays(2)));
            service.lend(loan(thirdId,BorrowerType.TEACHER,1,TODAY.minusDays(3),TODAY.minusDays(1)));
            check(service.findOverdueLoans(TODAY).size()==2 && service.findOverdueLoans(TODAY).stream().map(ResourceLoan::getBorrowerType).distinct().count()==2,"Overdue report includes students and teachers");
            rejected(() -> service.getResource(99999));rejected(() -> service.getLoan(BorrowerType.TEACHER,99999));
        }
        System.out.println("Passed "+checks+" resource JDBC checks using temporary MySQL tables.");
    }
}
