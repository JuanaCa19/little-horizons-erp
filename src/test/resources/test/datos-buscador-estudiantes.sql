-- Ejecutar después de docs/database.sql en una base de pruebas.
START TRANSACTION;

INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre,
                        primer_apellido, segundo_apellido, fecha_nacimiento, genero, fecha_ingreso)
SELECT 'TI', datos.documento, datos.nombre, datos.segundo_nombre, datos.apellido,
       datos.segundo_apellido, '2009-05-15', datos.genero, '2025-01-15'
FROM (
    SELECT 'DEMO-BUSCADOR-001' AS documento, 'Juan' AS nombre, 'Carlos' AS segundo_nombre,
           'Pérez' AS apellido, 'González' AS segundo_apellido, 'M' AS genero
    UNION ALL SELECT 'DEMO-BUSCADOR-002', 'María', 'Elena', 'López', 'Ramírez', 'F'
    UNION ALL SELECT 'DEMO-BUSCADOR-003', 'Carlos', NULL, 'Gómez', NULL, 'M'
) AS datos
WHERE NOT EXISTS (
    SELECT 1 FROM estudiantes e WHERE e.tipo_documento = 'TI' AND e.numero_documento = datos.documento
);

INSERT INTO matriculas (id_estudiante, id_grado, anio_academico, estado_matricula, fecha_matricula)
SELECT e.id_estudiante, g.id_grado, 2025, 'Activa', '2025-01-15'
FROM estudiantes e
JOIN grados g ON g.nivel_numerico = 10
WHERE e.tipo_documento = 'TI'
  AND e.numero_documento IN ('DEMO-BUSCADOR-001', 'DEMO-BUSCADOR-002', 'DEMO-BUSCADOR-003')
  AND NOT EXISTS (
      SELECT 1 FROM matriculas m WHERE m.id_estudiante = e.id_estudiante AND m.anio_academico = 2025
  );

COMMIT;
