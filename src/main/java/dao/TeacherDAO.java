package dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import model.*;
import model.enums.*;

public interface TeacherDAO {
    int create(Teacher teacher);
    Optional<Teacher> findById(Integer id);
    Optional<Teacher> findById(Integer id, Connection connection);
    Optional<Teacher> findByDocument(String number, DocumentType type);
    Optional<Teacher> findByEmail(String email);
    List<Teacher> findAll();
    List<Teacher> searchByName(String name);
    void update(Teacher teacher);
    void updateStatus(Integer id, TeacherStatus status);
    void lock(Integer id, Connection connection);
}
