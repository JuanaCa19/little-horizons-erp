package test.controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import test.modelo.EstudianteMatriculado;
import test.modelo.Grado;
import util.ConnectionDB;

public class BuscadorEstudiantes {

    public static final int ANIO_ACADEMICO = 2025;

    public List<Grado> consultarGrados() throws SQLException {
        String sql = """
                SELECT id_grado, nombre_grado, nivel_numerico
                FROM grados
                WHERE nivel_numerico BETWEEN 0 AND 11
                ORDER BY nivel_numerico
                """;
        List<Grado> grados = new ArrayList<>();
        try (Connection conexion = abrirConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql);
                ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                grados.add(new Grado(filas.getInt("id_grado"), filas.getString("nombre_grado"),
                        filas.getInt("nivel_numerico")));
            }
        }
        return grados;
    }

    public List<EstudianteMatriculado> buscarPorGrado(Grado grado) throws SQLException {
        String sql = """
                SELECT e.id_estudiante, e.tipo_documento, e.numero_documento,
                       e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido,
                       g.id_grado
                FROM estudiantes e
                JOIN matriculas m ON m.id_estudiante = e.id_estudiante
                JOIN grados g ON g.id_grado = m.id_grado
                WHERE m.anio_academico = ? AND m.estado_matricula = 'Activa'
                ORDER BY e.id_estudiante
                """;
        List<EstudianteMatriculado> matriculados = new ArrayList<>();
        try (Connection conexion = abrirConexion();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, ANIO_ACADEMICO);
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    matriculados.add(new EstudianteMatriculado(
                            filas.getInt("id_estudiante"), filas.getString("tipo_documento"),
                            filas.getString("numero_documento"), filas.getString("primer_nombre"),
                            filas.getString("segundo_nombre"), filas.getString("primer_apellido"),
                            filas.getString("segundo_apellido"), filas.getInt("id_grado")));
                }
            }
        }
        return matriculados.stream()
                .filter(estudiante -> estudiante.idGrado() == grado.id())
                .collect(Collectors.toList());
    }

    private Connection abrirConexion() throws SQLException {
        Connection conexion = ConnectionDB.getConnection();
        if (conexion == null) {
            throw new SQLException("No se pudo abrir la conexión con la base de datos.");
        }
        return conexion;
    }
}
