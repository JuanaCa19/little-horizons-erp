# Little Horizons ERP

Sistema de gestión educativa para el Colegio Internacional **Little Horizons**, desarrollado en Java con interfaz de consola, arquitectura MVC y persistencia en MySQL.

## 1. Descripción

El proyecto centraliza la información académica, administrativa y financiera del colegio. Permite gestionar estudiantes, acudientes, profesores, materias, actividades extracurriculares, pagos y recursos prestables desde una misma aplicación.

Su propósito es reducir la dispersión de registros, los errores de matrícula y calificaciones, la falta de seguimiento de pagos y la pérdida de trazabilidad de los préstamos.

La aplicación cuenta con siete módulos y consultas estadísticas en consola. **La generación de facturas y la exportación de documentos PDF quedaron excluidas del alcance por indicación del instructor.** No son funcionalidades pendientes de implementación.

## 2. Autor

**Juan Esteban Cárdenas Rivera**

- Autor y desarrollador del proyecto.
- Correo electrónico: no publicado.
- Contacto: mediante los canales del repositorio y del entorno académico del proyecto.

## 3. Tecnologías utilizadas

| Tecnología | Uso |
| --- | --- |
| Java 23 | Lenguaje y versión de compilación configurada en el proyecto |
| Apache Maven | Gestión del proyecto, compilación y dependencias |
| MySQL 8.0.16 o superior | Persistencia, integridad referencial y restricciones `CHECK` |
| MySQL Connector/J 8.4.0 | Controlador JDBC declarado en `pom.xml` |
| JDBC | Consultas parametrizadas, transacciones y acceso a datos |
| Java Stream API y lambdas | Procesamiento de colecciones y construcción de resultados |
| Git | Control de versiones |

El proyecto puede abrirse en Apache NetBeans, IntelliJ IDEA u otro IDE con soporte para Maven y JDK 23. La interfaz es de consola; no requiere un servidor web.

## 4. Requerimientos previos

- JDK **23**, con `java` y `javac` disponibles.
- Apache Maven **3.9 o superior**, o la instalación de Maven integrada en el IDE.
- MySQL **8.0.16 o superior**, con el servicio iniciado. Se necesita esta versión para aplicar los `CHECK` del esquema.
- Una herramienta para ejecutar el script SQL, como MySQL Workbench o el cliente `mysql`.
- Acceso a Maven Central para resolver la dependencia JDBC en la primera compilación.
- Una terminal compatible con UTF-8 para visualizar correctamente tildes, tablas y bordes del menú.

Comprobación de herramientas:

```powershell
java -version
javac -version
mvn -version
```

Maven debe utilizar el JDK 23. Si muestra otra versión, revisa `JAVA_HOME` y la configuración del IDE.

## 5. Configuración de la base de datos

### 5.1. Crear e importar el esquema

La aplicación utiliza la base de datos `erp_little_horizons` y los nombres de tablas y columnas en español del script corregido del proyecto.

El archivo [docs/database.sql](docs/database.sql) incluye las 26 tablas con sus restricciones y los datos ficticios de prueba. Se ejecuta una sola vez sobre una base nueva o vacía; no elimina tablas ni migra instalaciones existentes.

En MySQL Workbench, abre el archivo y ejecuta el script completo. Como alternativa, abre el cliente desde la raíz del proyecto:

```powershell
mysql --default-character-set=utf8mb4 -u tu_usuario -p
```

Dentro del cliente MySQL:

```sql
SOURCE docs/database.sql;
SOURCE docs/verify_database.sql;
```

[docs/verify_database.sql](docs/verify_database.sql) consulta conteos y reglas de coherencia sin modificar registros. Los valores `cumple` deben ser 1 y los valores `inconsistencias`, 0.

Los datos corresponden al año académico **2026**, con referencia **2026-11-16**. Incluyen hermanos, becas, cargos pendientes, ausencias y préstamos vencidos. Los reportes que usan la fecha actual pueden variar con el tiempo.

| Tabla principal | Registros |
| --- | ---: |
| `estudiantes` | 40 |
| `acudientes` | 34 |
| `profesores` | 40 |
| `materias` | 48 |
| `registros_academicos` | 480 |
| `pagos` | 425 |
| `clubes` | 30 |
| `recursos` | 50 |

Las tablas de facturas se conservan vacías por compatibilidad; la facturación y los PDF están excluidos del alcance.

El esquema incluye, entre otras, las siguientes tablas:

| Área | Tablas |
| --- | --- |
| Estudiantes | `grados`, `estudiantes`, `acudientes`, `estudiante_acudiente`, `matriculas` |
| Académica | `materias`, `profesores`, `asignaciones_profesor`, `horarios_asignacion`, `inscripciones_materia`, `registros_academicos`, `asistencias` |
| Extracurriculares | `clubes`, `membresias_club`, `eventos`, `participantes_evento` |
| Financiera | `conceptos_pago`, `precios_estudiante`, `cargos_estudiante`, `pagos` |
| Recursos | `categorias_recurso`, `recursos`, `prestamos_estudiante`, `prestamos_profesor` |

Las tablas de facturación que pueda contener el script original no son utilizadas por la aplicación. No es necesario eliminarlas para ejecutar el proyecto.

### 5.2. Configurar las credenciales

Desde la raíz del proyecto, copia el archivo de ejemplo:

```powershell
Copy-Item config.example.txt config.txt
```

Edita `config.txt` con los datos de tu instalación:

```properties
user=tu_usuario
password=tu_contrasena
url=jdbc:mysql://localhost:3306/erp_little_horizons
```

Conserva **ese orden** y esas tres entradas: el cargador actual obtiene los valores por posición. No agregues otras propiedades con `=` a este archivo.

La aplicación busca `config.txt` en su directorio de trabajo. Ejecuta los comandos desde la carpeta que contiene `pom.xml` y configura esa misma carpeta como directorio de trabajo en el IDE.

`config.txt` está excluido mediante `.gitignore`; las credenciales locales no forman parte de la configuración pública del repositorio.

### 5.3. Instalar el controlador JDBC

Maven resuelve MySQL Connector/J mediante la dependencia de [pom.xml](pom.xml):

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>
```

No es necesario copiar manualmente el controlador al código fuente. Para ejecutar desde una terminal, el JAR debe estar incluido en el classpath, como se muestra en la siguiente sección.

## 6. Instalación y ejecución

### 6.1. Clonar el repositorio

Utiliza la URL real del repositorio a la que tengas acceso:

```text
git clone <URL_DEL_REPOSITORIO>
cd little-horizons-erp
```

### 6.2. Abrir en el IDE

1. Abre la carpeta como proyecto Maven o selecciona su `pom.xml`.
2. Configura el JDK 23 para el proyecto.
3. Espera a que Maven resuelva las dependencias.
4. Importa el esquema en MySQL y prepara `config.txt`.
5. Selecciona `Main` como clase principal.

### 6.3. Compilar

```powershell
mvn clean compile
```

Para compilar y copiar las dependencias necesarias para la ejecución por terminal:

```powershell
mvn clean compile dependency:copy-dependencies
```

### 6.4. Ejecutar

Desde la raíz del proyecto, en Windows:

```powershell
java -cp "target/classes;target/dependency/*" Main
```

En Linux o macOS, el separador del classpath es `:`:

```bash
java -cp "target/classes:target/dependency/*" Main
```

También puedes ejecutar `Main.java` desde el IDE. El proyecto no configura un JAR ejecutable con dependencias; el comando de ejecución documentado utiliza el classpath.

## 7. Uso de la consola

El menú principal contiene:

```text
1. Gestion de Estudiantes
2. Gestion Academica
3. Gestion de Profesores
4. Actividades Extracurriculares
5. Gestion Financiera
6. Gestion de Recursos
7. Reportes y Estadisticas
8. Salir
```

- Selecciona una opción escribiendo su número.
- Usa la opción **Volver** de cada submenú para regresar.
- Escribe `cancelar` en las entradas de formularios para cancelar la operación actual.
- Introduce las fechas como `dd/MM/yyyy` y las horas como `HH:mm`.
- Los campos opcionales pueden quedar vacíos al crear un registro.
- En los formularios de edición que lo indican, Enter conserva los textos actuales y `-` borra un campo opcional.
- Las operaciones de escritura solicitan confirmación antes de guardar.
- Las validaciones de entrada permiten reintentar; los errores de negocio regresan al menú correspondiente.

## 8. Estructura y arquitectura

```text
little-horizons-erp/
├── pom.xml
├── config.example.txt
├── scripts/test.ps1
├── docs/
│   ├── database.sql
│   ├── verify_database.sql
│   ├── financial-module.md
│   ├── resource-module.md
│   └── reports-module.md
└── src/
    ├── main/java/
    │   ├── Main.java
    │   ├── model/
    │   │   ├── enums/
    │   │   └── report/
    │   ├── dao/
    │   │   └── impl/
    │   ├── service/
    │   ├── controller/
    │   ├── view/
    │   └── util/
    │       └── exception/
    └── test/java/
        ├── service/
        ├── integration/
        └── view/
```

El flujo principal sigue esta separación:

```text
Consola → Vista → Controlador → Servicio → DAO → MySQL
```

| Paquete | Responsabilidad |
| --- | --- |
| `model` | Entidades del dominio, enumeraciones y resultados de reportes |
| `view` | Menús, formularios, tablas y mensajes para el usuario |
| `controller` | Operaciones que las vistas invocan para acceder a los servicios |
| `service` | Reglas de negocio, validaciones y coordinación de transacciones |
| `dao` | Contratos para acceder a los datos |
| `dao.impl` | Implementaciones JDBC y mapeo entre tablas en español y objetos en inglés |
| `util` | Lectura de entradas, presentación de consola, configuración, conexión y logs |
| `util.exception` | Excepciones de validación, acceso a datos y cancelación |

Los servicios no leen datos de la consola. Las vistas no ejecutan SQL. Las operaciones compuestas comparten una conexión para guardar o revertir sus cambios conjuntamente.

## 9. Principios y patrones de diseño

### 9.1. Principios SOLID

| Principio | Aplicación en el proyecto |
| --- | --- |
| Responsabilidad única | `InputReader` lee entradas, `AppLogger` registra errores y los DAO ejecutan consultas. Las reglas de matrícula, pagos y préstamos están en servicios. |
| Abierto/cerrado | Las implementaciones de persistencia pueden sustituirse mediante los contratos DAO sin modificar los formularios. |
| Sustitución de Liskov | Los servicios consumen contratos como `GuardianDAO`; las implementaciones JDBC y los dobles de prueba deben respetar sus operaciones y resultados. |
| Segregación de interfaces | Los contratos se agrupan por responsabilidad. Por ejemplo, `GradeDAO` ofrece consultas y `ReportDAO` consultas estadísticas, sin imponerles operaciones de escritura. |
| Inversión de dependencias | `GuardianService` recibe un `GuardianDAO` por constructor. Otros servicios permiten inyectar sus DAO y el proveedor de conexiones para probarlos de forma aislada. |

Ejemplos concretos:

- [GuardianService](src/main/java/service/GuardianService.java): dependencia de una interfaz DAO e inyección por constructor.
- [StudentService](src/main/java/service/StudentService.java): registro compuesto del estudiante con acudiente, vínculo y matrícula.
- [ResourceService](src/main/java/service/ResourceService.java): disponibilidad, préstamos y devoluciones.
- [ReportDAO](src/main/java/dao/ReportDAO.java): contrato de consultas de solo lectura.

### 9.2. Patrones y técnicas utilizados

- **MVC:** separa presentación, operaciones de control y modelo; se complementa con servicios y DAO.
- **DAO:** concentra la persistencia y evita que las vistas conozcan las consultas SQL.
- **Capa de servicios:** reúne las reglas de negocio y los casos de uso.
- **Inyección por constructor:** permite reemplazar dependencias en las pruebas.
- **Transacciones:** `TransactionManager` coordina `commit` y `rollback` para operaciones que deben completarse juntas.
- **Mapeo de filas:** los DAO transforman `ResultSet` en entidades mediante métodos de mapeo.
- **Enumeraciones:** relacionan valores del dominio en inglés con los valores guardados en MySQL.

Se utilizan lambdas y Stream API, por ejemplo en `Student.getFullName()`, filtros de reportes y construcción de filas para las tablas de consola.

## 10. Funcionalidades principales

### 10.1. Estudiantes y acudientes

- Registro completo del estudiante con acudiente nuevo o existente y matrícula inicial.
- Consultas por identificador, documento y nombre; edición de datos y cambios de estado.
- Relación de varios estudiantes con un acudiente y de varios acudientes con un estudiante.
- Identificación de acudiente principal, responsable financiero y contacto de emergencia.
- Protección del último acudiente y cambio transaccional del acudiente principal.
- Historial de matrículas por año y grado.

### 10.2. Gestión académica

- Registro y actualización de materias por grado.
- Inscripciones por estudiante, materia, año, período y sección.
- Validación de correspondencia entre la materia y el grado matriculado.
- Registro y corrección de notas entre **0,0 y 5,0**, con máximo un decimal.
- Verificación de la asignación del profesor a la materia y sección.
- Historial y promedios por estudiante, materia, período y curso.
- Registro y corrección de asistencia; consultas por fechas y alertas semanales.

### 10.3. Profesores

- Directorio, búsquedas, registro y actualización.
- Estados Activo, Inactivo y Licencia.
- Asignación de materias por año académico y sección.
- Horarios y detección de cruces del profesor o del grupo.
- Consulta de carga académica.

### 10.4. Actividades extracurriculares

- Creación y edición de clubes con capacidad y profesor encargado.
- Inscripciones con validación transaccional de cupos.
- Retiros y reingresos con conservación del historial.
- Calendario de eventos, participantes, roles y resultados de competencias.
- Validación de participaciones duplicadas y posiciones obtenidas.

### 10.5. Gestión financiera

- Conceptos de pago, valores base e IVA.
- Precios personalizados por estudiante para becas y descuentos, con vigencias que no se cruzan.
- Creación de cargos, consulta de pendientes y anulación.
- Pago completo de uno o varios cargos, incluso de varios estudiantes a cargo del mismo responsable financiero.
- Recibos únicos e historial por estudiante y acudiente.
- Consulta de morosos con más de un mes de atraso.

Los montos utilizan `BigDecimal`. Los pagos y el estado de sus cargos se guardan en una misma transacción. La facturación y los PDF están fuera del alcance acordado.

### 10.6. Gestión de recursos

- Categorías y catálogo de libros, equipos y otros recursos prestables.
- Búsquedas y filtros por nombre, código, categoría y estado.
- Préstamos a estudiantes y profesores en sus tablas separadas.
- Control de disponibilidad entre ambas tablas de préstamos.
- Devoluciones con fecha real y actualización de disponibilidad.
- Historial por recurso y solicitante; consultas de préstamos vencidos.

### 10.7. Reportes y estadísticas

- Resumen general del colegio y matrícula por grado.
- Promedios, estudiantes que requieren apoyo y carga docente.
- Asistencia e inasistencias semanales.
- Recaudos, cartera pendiente y mora.
- Ocupación de clubes y participación extracurricular.
- Inventario y préstamos vencidos.

Todas estas consultas son de solo lectura y se muestran en consola.

## 11. Reglas de cálculo y conservación del historial

- El promedio de un estudiante por materia usa la nota Final cuando existe; de lo contrario, promedia los períodos registrados.
- Los reportes por período no combinan las notas Final con I, II, III y IV. La ausencia de notas no se interpreta como una calificación de cero.
- Las alertas semanales cuentan días distintos de ausencia, aunque se haya faltado a varias materias el mismo día.
- El porcentaje de asistencia incluye presente y tardanza; excluye las excusas médicas del denominador.
- Los precios personalizados se evalúan al primer día del mes causado. Cambiarlos no reescribe el valor base de los cargos existentes.
- La mora se calcula para cargos positivos pendientes con vencimiento anterior a la fecha de referencia menos un mes.
- Un préstamo que vence hoy no se considera vencido hasta el día siguiente.
- Las consultas de cartera y préstamos muestran las obligaciones actualmente abiertas; no reconstruyen balances históricos completos.
- Estudiantes, recursos y actividades se gestionan mediante estados. Los retiros y devoluciones conservan su historial.

Documentación complementaria: [finanzas](docs/financial-module.md), [recursos](docs/resource-module.md) y [reportes](docs/reports-module.md).

## 12. Manejo de errores y logs

La aplicación distingue:

| Excepción | Propósito |
| --- | --- |
| `ValidationException` | Comunicar datos o reglas de negocio incumplidas |
| `DataAccessException` | Comunicar fallos de persistencia |
| `CancelledException` | Interrumpir un formulario a petición del usuario |

Las vistas muestran mensajes de validación y error y permiten continuar la navegación. Los errores inesperados y los fallos SQL registrados por los DAO se envían a `AppLogger`.

El archivo **`error_log.txt`** se crea o amplía en el directorio de trabajo, con fecha, hora, contexto y excepción. Actualmente el logger guarda un resumen del error; no almacena la traza completa de llamadas. El archivo está excluido mediante `.gitignore`.

Los DAO emplean `PreparedStatement` y `try-with-resources`. Las operaciones compuestas utilizan transacciones; ante un fallo, sus cambios se revierten mediante `rollback`.

## 13. Pruebas y verificación

La batería se puede ejecutar desde PowerShell con un solo comando:

```powershell
./scripts/test.ps1
```

El script compila todas las clases, ejecuta las pruebas de servicios, las siete suites JDBC y todos los escenarios de consola en JVM separadas. Se detiene ante cualquier fallo y conserva las clases compiladas en una carpeta de verificación dentro de `target/`. Utiliza JDK 23 y el driver JDBC del repositorio local de Maven.

Para ejecutar los servicios y escenarios de consola sin MySQL:

```powershell
./scripts/test.ps1 -SkipDatabase
```

Si el driver está en otra ubicación:

```powershell
./scripts/test.ps1 -JdbcJar 'target/dependency/mysql-connector-j-8.4.0.jar'
```

Las pruebas son clases ejecutables mediante `main`, sin un framework de pruebas. **`mvn test` no ejecuta automáticamente esta batería.** También pueden lanzarse explícitamente con los comandos siguientes.

Compila las clases y prepara las dependencias:

```powershell
mvn test-compile dependency:copy-dependencies
$testClasspath = "target/test-classes;target/classes;target/dependency/*"
```

Pruebas aisladas de servicios:

```powershell
java -cp $testClasspath service.StudentModuleServiceTest
```

Pruebas de integración JDBC:

```powershell
$integrationSuites = @(
    "integration.TeacherModuleIntegrationTest",
    "integration.AcademicModuleIntegrationTest",
    "integration.ExtracurricularModuleIntegrationTest",
    "integration.FinanceModuleIntegrationTest",
    "integration.ResourceModuleIntegrationTest",
    "integration.ReportModuleIntegrationTest"
)

foreach ($suite in $integrationSuites) {
    java -cp $testClasspath $suite
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo en $suite"
    }
}
```

Estas pruebas requieren MySQL disponible, el esquema completo importado, `config.txt` y permisos para crear tablas temporales. Utilizan tablas de sesión que ocultan las tablas reales durante la prueba y no modifican sus registros.

Las tablas temporales conservan columnas, índices y restricciones de comprobación del esquema, pero omiten las llaves foráneas para aislar los escenarios. Por eso estas pruebas no sustituyen la verificación final del script completo y de su integridad referencial en una base limpia.

### Verificar la instalación SQL completa

```powershell
java -cp $testClasspath integration.DatabaseScriptIntegrationTest
```

Esta prueba requiere permisos CREATE y DROP DATABASE. Importa los scripts con las llaves foráneas y CHECK reales en una base de nombre aleatorio, valida los resultados y elimina exclusivamente esa base al terminar. No modifica los registros de la base configurada.


Ejemplos de pruebas de consola:

```powershell
java -cp $testClasspath view.ReportViewFlowTest navigation
java -cp $testClasspath view.FinanceViewFlowTest pay
java -cp $testClasspath view.ResourceViewFlowTest return-student
```

Cada escenario de consola debe ejecutarse en una JVM nueva, porque `InputReader` utiliza un `Scanner` compartido. Las clases de `src/test/java/view` incluyen escenarios de navegación, cancelación, confirmación, validación y presentación de resultados.

Antes de entregar, comprueba también el registro completo de estudiantes y los flujos principales desde la consola contra una base recién instalada.

## 14. Problemas conocidos y limitaciones

- La configuración de conexión depende del orden de las entradas de `config.txt` y del directorio de trabajo.
- No existe autenticación ni control de acceso por roles. La selección de un profesor por identificador no equivale a iniciar sesión como ese profesor.
- Los horarios detectan cruces básicos; no se administra un calendario escolar complejo ni se asignan automáticamente las fechas de cada período.
- No se gestionan abonos parciales sobre un mismo cargo; el pago salda cargos completos.
- No se han definido ponderaciones distintas para materias o períodos: los promedios utilizan las notas disponibles según las reglas descritas.
- Las pruebas se automatizan con `scripts/test.ps1`; todavía no están integradas al ciclo de Maven ni a un flujo de integración continua.
- El logger actual guarda un resumen y no la traza completa de los errores.

## 15. Futuras mejoras

- Integrar la batería de pruebas al ciclo de Maven y a integración continua.
- Incorporar autenticación y permisos por perfil.
- Mejorar la configuración para identificar propiedades por nombre.
- Añadir ponderaciones académicas configurables y un calendario de períodos.
- Ampliar el registro técnico de errores y optimizar consultas que recuperan datos relacionados por separado.

## 16. Licencia y contacto

La licencia está por definir por el autor. Actualmente el repositorio no incluye un archivo `LICENSE`.

**Autor:** Juan Esteban Cárdenas Rivera. Para consultas, utiliza los canales del repositorio o del entorno académico. No se publica un correo personal en esta documentación.

## 17. Entrega en GitHub

Antes de entregar en la plataforma:

- Verifica que el repositorio sea privado.
- Agrega al instructor como colaborador con acceso de lectura.
- Realiza tus commits con mensajes descriptivos en inglés y deja la versión final en main o master.
- Copia la URL del repositorio y el SHA completo del commit final.

Para consultar el SHA después de realizar tu commit:

```powershell
git rev-parse HEAD
```

La privacidad, los colaboradores y la entrega en la plataforma se verifican en GitHub; las pruebas locales no comprueban esos requisitos.
