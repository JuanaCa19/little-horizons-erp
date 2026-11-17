package dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import model.*;
import model.enums.*;

public interface SubjectDAO {
    Optional<Subject> findById(Integer id);
    List<Subject> findAll();
    int create(Subject subject);
    Optional<Subject> findByCode(String code);
    Optional<Subject> findByNameAndGrade(String name, Integer gradeId);
    boolean hasUsage(Integer subjectId);
    void update(Subject subject);
}
