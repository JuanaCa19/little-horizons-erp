package model;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record StudentEnrollmentSummary(int id, String documentType, String documentNumber,
        String firstName, String middleName, String firstLastName, String secondLastName,
        int gradeId) {

    public String fullName() {
        return Stream.of(firstName, middleName, firstLastName, secondLastName)
                .filter(part -> part != null && !part.isBlank())
                .map(String::strip)
                .collect(Collectors.joining(" "));
    }
}
