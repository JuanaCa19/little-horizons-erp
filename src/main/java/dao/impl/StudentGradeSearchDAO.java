package dao.impl;

import java.util.List;
import model.StudentEnrollmentSummary;

public class StudentGradeSearchDAO {
    private final JdbcSupport jdbc = new JdbcSupport();

    public List<StudentEnrollmentSummary> findActiveEnrollments(int academicYear) {
        String sql = """
                SELECT e.id_estudiante, e.tipo_documento, e.numero_documento,
                       e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido,
                       m.id_grado
                FROM estudiantes e
                JOIN matriculas m ON m.id_estudiante = e.id_estudiante
                WHERE m.anio_academico = ? AND m.estado_matricula = 'Activa'
                ORDER BY e.id_estudiante
                """;
        return jdbc.execute(connection -> JdbcSupport.query(connection, sql,
                statement -> statement.setInt(1, academicYear),
                row -> new StudentEnrollmentSummary(
                        row.getInt("id_estudiante"), row.getString("tipo_documento"),
                        row.getString("numero_documento"), row.getString("primer_nombre"),
                        row.getString("segundo_nombre"), row.getString("primer_apellido"),
                        row.getString("segundo_apellido"), row.getInt("id_grado"))));
    }
}
