package controller;

import dao.GradeDAO;
import dao.impl.GradeDAOImpl;
import dao.impl.StudentGradeSearchDAO;
import java.util.List;
import java.util.stream.Collectors;
import model.Grade;
import model.StudentEnrollmentSummary;

public class StudentGradeSearchController {
    public static final int ACADEMIC_YEAR = 2025;
    private final GradeDAO grades = new GradeDAOImpl();
    private final StudentGradeSearchDAO students = new StudentGradeSearchDAO();

    public List<Grade> findGrades() {
        return grades.findAll();
    }

    public List<StudentEnrollmentSummary> searchByGrade(int gradeId) {
        return students.findActiveEnrollments(ACADEMIC_YEAR).stream()
                .filter(student -> student.gradeId() == gradeId)
                .collect(Collectors.toList());
    }
}
