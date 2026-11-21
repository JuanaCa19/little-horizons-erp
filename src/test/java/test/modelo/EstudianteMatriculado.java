package test.modelo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record EstudianteMatriculado(int id, String tipoDocumento, String numeroDocumento,
        String primerNombre, String segundoNombre, String primerApellido, String segundoApellido,
        int idGrado) {

    public String nombreCompleto() {
        return Stream.of(primerNombre, segundoNombre, primerApellido, segundoApellido)
                .filter(parte -> parte != null && !parte.isBlank())
                .map(String::strip)
                .collect(Collectors.joining(" "));
    }
}
