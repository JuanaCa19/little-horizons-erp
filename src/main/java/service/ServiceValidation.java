package service;

import model.Enrollment;
import model.Guardian;
import model.Student;
import model.StudentGuardian;
import util.exception.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

final class ServiceValidation {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ServiceValidation() {
    }

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    static void id(Integer id) {
        require(id != null && id > 0, "El identificador debe ser mayor que cero.");
    }

    static String text(String value, String field) {
        require(value != null && !value.isBlank(), field + " es obligatorio.");
        return value.trim();
    }

    static void email(String value) {
        require(value == null || value.isBlank() || EMAIL.matcher(value).matches(),
                "El correo electronico no es valido.");
    }

    static void student(Student student) {
        require(student != null, "El estudiante es obligatorio.");
        require(student.getDocumentType() != null, "El tipo de documento es obligatorio.");
        String doc = text(student.getDocumentNumber(), "El documento");
        require(doc.length() <= 20, "El documento no puede superar 20 caracteres.");
        text(student.getFirstName(), "El primer nombre");
        text(student.getFirstLastName(), "El primer apellido");
        text(student.getAddress(), "La direccion");
        require(student.getBirthDate() != null && student.getBirthDate().isBefore(LocalDate.now()),
                "La fecha de nacimiento debe ser anterior a hoy.");
        require(student.getAdmissionDate() != null && !student.getAdmissionDate().isBefore(student.getBirthDate()),
                "La fecha de ingreso no puede ser anterior al nacimiento.");
        require(student.getGender() != null, "El genero es obligatorio.");
        require(student.getStatus() != null, "El estado del estudiante es obligatorio.");
        email(student.getEmail());
    }

    static void guardian(Guardian guardian) {
        require(guardian != null, "El acudiente es obligatorio.");
        require(guardian.getDocumentType() != null, "El tipo de documento es obligatorio.");
        String doc = text(guardian.getDocumentNumber(), "El documento");
        require(doc.length() <= 20, "El documento no puede superar 20 caracteres.");
        text(guardian.getFirstName(), "El primer nombre");
        text(guardian.getFirstLastName(), "El primer apellido");
        text(guardian.getPrimaryPhone(), "El telefono principal");
        text(guardian.getAddress(), "La direccion");
        email(guardian.getEmail());
    }

    static void relation(StudentGuardian relation) {
        require(relation != null, "El vinculo con el acudiente es obligatorio.");
        text(relation.getRelationShip(), "El parentesco");
    }

    static void enrollment(Enrollment enrollment) {
        require(enrollment != null, "La matricula es obligatoria.");
        id(enrollment.getGradeId());
        year(enrollment.getAcademicYear());
        require(enrollment.getEnrollmentDate() != null, "La fecha de matricula es obligatoria.");
        require(enrollment.getEnrollmentDate().getYear() <= enrollment.getAcademicYear(),
                "La fecha de matricula no puede ser posterior al anio academico.");
    }

    static void year(int year) {
        require(year >= 1900 && year <= 9999, "El anio academico debe estar entre 1900 y 9999.");
    }
}
