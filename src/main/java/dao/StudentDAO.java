package dao;

import model.DocumentType;
import model.Student;
import model.StudentStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {
    int create(Student student);

    int create(Student student, Connection connection);

    Optional<Student> findById(Integer id);

    Optional<Student> findByDocument(String documentNumber, DocumentType documentType);

    boolean existsByDocument(DocumentType documentType, String documentNumber);

    List<Student> findAll();

    List<Student> searchByName(String name);

    void update(Student student);

    void updateStatus(StudentStatus studentStatus, Integer id);
}
