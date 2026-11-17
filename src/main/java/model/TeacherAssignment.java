package model;

import java.time.*;
import model.enums.*;

public class TeacherAssignment {
    private Integer id;
    private Integer teacherId;
    private Integer subjectId;
    private int academicYear;
    private String section;

    public TeacherAssignment() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }


}
