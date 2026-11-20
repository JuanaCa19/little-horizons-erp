package test.vista;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import test.controlador.BuscadorEstudiantes;
import test.modelo.EstudianteMatriculado;
import test.modelo.Grado;

public class BuscadorEstudiantesMain {

    private static final String LINEA = "=".repeat(79);
    private static final String SEPARADOR = "-".repeat(79);
    private static final int ANIO = BuscadorEstudiantes.ANIO_ACADEMICO;

    public static void main(String[] args) {
        BuscadorEstudiantes buscador = new BuscadorEstudiantes();
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                mostrarMenu();
                String opcion = leer(entrada);
                if (opcion == null || opcion.equals("0")) {
                    System.out.println("Saliendo del buscador...\nGracias por usar el sistema.");
                    return;
                }
                switch (opcion) {
                    case "1" -> filtrarPorGrado(entrada, buscador);
                    case "2", "3", "4", "5" -> {
                        System.out.println("Esta funcionalidad aún no ha sido implementada.");
                        System.out.println("Será agregada en futuras versiones del sistema.");
                        pausar(entrada, "Presione Enter para continuar...");
                    }
                    default -> {
                        System.out.println("Error: Seleccione una opción entre 0 y 5.");
                        pausar(entrada, "Presione Enter para continuar...");
                    }
                }
            }
        }
    }

    private static void mostrarMenu() {
        encabezado("BUSCADOR AVANZADO DE ESTUDIANTES - PROTOTIPO");
        System.out.print("""

                Seleccione el filtro que desea aplicar:

                1. Filtrar por grado académico [FUNCIONAL]
                2. Filtrar por rango de edad [NO IMPLEMENTADO]
                3. Filtrar por género [NO IMPLEMENTADO]
                4. Filtrar por estado de matrícula [NO IMPLEMENTADO]
                5. Filtrar por rango de promedio académico [NO IMPLEMENTADO]
                0. Salir

                """);
        System.out.print("Seleccione una opción: ");
    }

    private static void filtrarPorGrado(Scanner entrada, BuscadorEstudiantes buscador) {
        try {
            List<Grado> grados = buscador.consultarGrados();
            if (grados.isEmpty()) {
                System.out.println("No hay grados académicos disponibles en la base de datos.");
            } else {
                Grado grado = seleccionarGrado(entrada, grados);
                if (grado == null) {
                    return;
                }
                System.out.println("\nProcesando búsqueda...");
                System.out.printf("Consultando estudiantes matriculados en %s...%n", grado.nombre());
                mostrarResultados(grado, buscador.buscarPorGrado(grado));
            }
        } catch (SQLException ex) {
            System.out.println("Error al consultar la base de datos: " + ex.getMessage());
        }
        pausar(entrada, "Presione Enter para volver al menú principal...");
        System.out.println(LINEA);
    }

    private static Grado seleccionarGrado(Scanner entrada, List<Grado> grados) {
        System.out.println("\n--- FILTRAR POR GRADO ACADÉMICO ---\n");
        System.out.println("Grados disponibles en el sistema:");
        for (Grado grado : grados) {
            System.out.printf("%d. %s%n", grado.nivel() + 1, grado.nombre());
        }

        while (true) {
            System.out.print("\nIngrese el número del grado que desea filtrar (o 0 para cancelar): ");
            String texto = leer(entrada);
            if (texto == null) {
                return null;
            }
            try {
                int opcion = Integer.parseInt(texto);
                if (opcion == 0) {
                    System.out.println("Operación cancelada.");
                    pausar(entrada, "Presione Enter para volver al menú principal...");
                    return null;
                }
                if (opcion < 1 || opcion > 12) {
                    System.out.println("Error: Debe seleccionar un número entre 1 y 12.");
                } else {
                    for (Grado grado : grados) {
                        if (grado.nivel() + 1 == opcion) {
                            return grado;
                        }
                    }
                    System.out.println("Error: El grado seleccionado no está disponible.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Error: Debe ingresar un número válido.");
            }
            pausar(entrada, "Presione Enter para intentar nuevamente...");
        }
    }

    private static void mostrarResultados(Grado grado, List<EstudianteMatriculado> estudiantes) {
        encabezado("RESULTADOS DE LA BÚSQUEDA");
        System.out.printf("%nFiltro aplicado: Grado académico = %s%n%n", grado.nombre());
        if (estudiantes.isEmpty()) {
            System.out.printf("No se encontraron estudiantes matriculados en %s para el año %d.%n",
                    grado.nombre(), ANIO);
            System.out.println("\nPosibles razones:");
            System.out.printf("  - No hay estudiantes matriculados en ese grado para el año %d%n", ANIO);
            System.out.printf("  - Verifique que la base de datos tenga información del año %d%n", ANIO);
        } else {
            int total = estudiantes.size();
            if (total == 1) {
                System.out.printf("Se encontró 1 estudiante matriculado en %s (año %d):%n%n",
                        grado.nombre(), ANIO);
            } else {
                System.out.printf("Se encontraron %d estudiantes matriculados en %s (año %d):%n%n",
                        total, grado.nombre(), ANIO);
            }
            System.out.println(SEPARADOR);
            for (EstudianteMatriculado estudiante : estudiantes) {
                System.out.printf("ID: %d%nNombre: %s%nDocumento: %s %s%n", estudiante.id(),
                        estudiante.nombreCompleto(), estudiante.tipoDocumento(), estudiante.numeroDocumento());
                System.out.println(SEPARADOR);
            }
            System.out.printf("%nTotal de resultados: %d %s%n", total,
                    total == 1 ? "estudiante" : "estudiantes");
        }
        System.out.println("\n" + LINEA);
    }

    private static void encabezado(String titulo) {
        System.out.println("\n" + LINEA);
        System.out.println("            " + titulo);
        System.out.println(LINEA);
    }

    private static void pausar(Scanner entrada, String mensaje) {
        System.out.println(mensaje);
        leer(entrada);
    }

    private static String leer(Scanner entrada) {
        return entrada.hasNextLine() ? entrada.nextLine().strip() : null;
    }
}
