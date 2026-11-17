-- Solo lectura: cumple debe ser 1 e inconsistencias debe ser 0.
USE erp_little_horizons;

SELECT 'estudiantes' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM estudiantes UNION ALL
SELECT 'acudientes' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM acudientes UNION ALL
SELECT 'profesores' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM profesores UNION ALL
SELECT 'materias' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM materias UNION ALL
SELECT 'registros_academicos' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM registros_academicos UNION ALL
SELECT 'pagos' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM pagos UNION ALL
SELECT 'clubes' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM clubes UNION ALL
SELECT 'recursos' AS tabla,COUNT(*) AS registros,COUNT(*)>=30 AS cumple FROM recursos;

SELECT 'Estudiantes sin acudiente' AS validacion,(SELECT COUNT(*) FROM estudiantes e WHERE NOT EXISTS(SELECT 1 FROM estudiante_acudiente a WHERE a.id_estudiante=e.id_estudiante)) AS inconsistencias;

SELECT 'Clubes sobre capacidad' AS validacion,(SELECT COUNT(*) FROM clubes c WHERE (SELECT COUNT(*) FROM membresias_club m WHERE m.id_club=c.id_club AND m.estado_membresia='Activa')>c.capacidad_maxima) AS inconsistencias;

SELECT 'Notas sin inscripcion' AS validacion,(SELECT COUNT(*) FROM registros_academicos r WHERE NOT EXISTS(SELECT 1 FROM inscripciones_materia i WHERE i.id_estudiante=r.id_estudiante AND i.id_materia=r.id_materia AND i.anio_academico=r.anio_academico AND i.periodo_academico=IF(r.periodo_academico='Final','IV',r.periodo_academico))) AS inconsistencias;

SELECT 'Pagos sin responsable financiero' AS validacion,(SELECT COUNT(*) FROM pagos p WHERE NOT EXISTS(SELECT 1 FROM estudiante_acudiente a WHERE a.id_estudiante=p.id_estudiante AND a.id_acudiente=p.id_acudiente AND a.es_responsable_financiero=1)) AS inconsistencias;

SELECT 'Prestamos abiertos duplicados' AS validacion,(SELECT COUNT(*) FROM (SELECT id_recurso FROM (SELECT id_recurso FROM prestamos_estudiante WHERE estado_prestamo<>'Devuelto' UNION ALL SELECT id_recurso FROM prestamos_profesor WHERE estado_prestamo<>'Devuelto') p GROUP BY id_recurso HAVING COUNT(*)>1) q) AS inconsistencias;

SELECT 'Cruces de profesor' AS validacion,(SELECT COUNT(*) FROM horarios_asignacion h JOIN horarios_asignacion j ON h.id_horario<j.id_horario AND h.dia_semana=j.dia_semana AND h.hora_inicio<j.hora_fin AND j.hora_inicio<h.hora_fin JOIN asignaciones_profesor a ON a.id_asignacion=h.id_asignacion JOIN asignaciones_profesor b ON b.id_asignacion=j.id_asignacion WHERE a.anio_academico=b.anio_academico AND a.id_profesor=b.id_profesor) AS inconsistencias;

SELECT 'Acudiente principal distinto de uno' AS validacion,(SELECT COUNT(*) FROM estudiantes e WHERE (SELECT COUNT(*) FROM estudiante_acudiente a WHERE a.id_estudiante=e.id_estudiante AND a.es_principal=1)<>1) AS inconsistencias;

SELECT 'Inscripciones sin matricula del grado' AS validacion,(SELECT COUNT(*) FROM inscripciones_materia i JOIN materias s ON s.id_materia=i.id_materia WHERE NOT EXISTS(SELECT 1 FROM matriculas m WHERE m.id_estudiante=i.id_estudiante AND m.anio_academico=i.anio_academico AND m.id_grado=s.id_grado)) AS inconsistencias;

SELECT 'Notas sin profesor asignado' AS validacion,(SELECT COUNT(*) FROM registros_academicos r WHERE NOT EXISTS(SELECT 1 FROM asignaciones_profesor a WHERE a.id_profesor=r.id_profesor AND a.id_materia=r.id_materia AND a.anio_academico=r.anio_academico)) AS inconsistencias;

SELECT 'Estado de recurso incoherente' AS validacion,(SELECT COUNT(*) FROM recursos r WHERE (r.estado_recurso='Prestado')<>(EXISTS(SELECT 1 FROM prestamos_estudiante p WHERE p.id_recurso=r.id_recurso AND p.estado_prestamo<>'Devuelto') OR EXISTS(SELECT 1 FROM prestamos_profesor p WHERE p.id_recurso=r.id_recurso AND p.estado_prestamo<>'Devuelto'))) AS inconsistencias;

SELECT 'Cargos pagados incoherentes' AS validacion,(SELECT COUNT(*) FROM cargos_estudiante c JOIN pagos p ON p.id_pago=c.id_pago JOIN conceptos_pago x ON x.id_concepto=c.id_concepto WHERE c.estado='Pagado' AND (c.id_estudiante<>p.id_estudiante OR c.id_concepto<>p.id_concepto OR c.anio_academico<>p.anio_academico OR c.periodo_academico<>p.periodo_academico OR p.monto_pagado<>c.monto_cargo+ROUND(c.monto_cargo*x.porcentaje_iva/100,2))) AS inconsistencias;

SELECT 'Precios con vigencia superpuesta' AS validacion,(SELECT COUNT(*) FROM precios_estudiante a JOIN precios_estudiante b ON a.id_precio<b.id_precio AND a.id_estudiante=b.id_estudiante AND a.id_concepto=b.id_concepto AND a.vigente_desde<=COALESCE(b.vigente_hasta,'9999-12-31') AND b.vigente_desde<=COALESCE(a.vigente_hasta,'9999-12-31')) AS inconsistencias;

SELECT 'Posiciones en eventos sin competencia' AS validacion,(SELECT COUNT(*) FROM participantes_evento p JOIN eventos e ON e.id_evento=p.id_evento WHERE e.tiene_competencia=0 AND p.posicion_obtenida IS NOT NULL) AS inconsistencias;

SELECT 'Cruces de horario del grupo' AS validacion,(SELECT COUNT(*) FROM horarios_asignacion h JOIN horarios_asignacion j ON h.id_horario<j.id_horario AND h.dia_semana=j.dia_semana AND h.hora_inicio<j.hora_fin AND j.hora_inicio<h.hora_fin JOIN asignaciones_profesor a ON a.id_asignacion=h.id_asignacion JOIN asignaciones_profesor b ON b.id_asignacion=j.id_asignacion JOIN materias s ON s.id_materia=a.id_materia JOIN materias t ON t.id_materia=b.id_materia WHERE a.anio_academico=b.anio_academico AND s.id_grado=t.id_grado AND a.seccion=b.seccion) AS inconsistencias;

