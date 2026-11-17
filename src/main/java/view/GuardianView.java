package view;

import controller.GuardianController;
import model.Guardian;
import model.enums.DocumentType;

import java.util.List;
import java.util.Objects;

import static util.ConsoleUI.printHeader;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printSuccess;
import static util.ConsoleUI.printTable;
import static util.InputReader.readRequiredText;
import static util.InputReader.readYesNo;
import static view.ViewSupport.action;
import static view.ViewSupport.choose;
import static view.ViewSupport.field;
import static view.ViewSupport.menu;
import static view.ViewSupport.optional;
import static view.ViewSupport.readId;
import static view.ViewSupport.required;
import static view.ViewSupport.row;

public class GuardianView {

    private final GuardianController controller;

    public GuardianView() {
        this(new GuardianController());
    }

    public GuardianView(GuardianController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    public void start() {
        menu("Gestion de Acudientes", "Volver",
                new String[]{
                        "Registrar",
                        "Listar",
                        "Buscar por nombre",
                        "Buscar por documento",
                        "Consultar detalle",
                        "Actualizar"
                },
                action(this::create),
                action(() -> showList(controller.findAll())),
                action(() -> showList(controller.searchByName(readRequiredText("Nombre o apellido: ")))),
                action(this::findByDocument),
                action(() -> showDetails(select())),
                action(this::update)
        );
    }

    public Guardian select() {
        Guardian guardian = controller.getById(readId("ID del acudiente"));
        field("Acudiente seleccionado", guardian.getFullName());
        return guardian;
    }

    public Guardian readForm(Guardian current) {
        printHeader(current == null ? "Datos del acudiente" : "Actualizar acudiente");
        if (current != null) {
            printInfo("Enter conserva los textos actuales. '-' borra los campos opcionales.");
        }

        Guardian guardian = new Guardian();
        guardian.setId(current == null ? null : current.getId());
        guardian.setDocumentType(choose("Tipo de documento", DocumentType.values(), DocumentType::getDbValue,
                current == null ? null : current.getDocumentType()));
        guardian.setDocumentNumber(required("Numero de documento", current == null ? null : current.getDocumentNumber()));
        guardian.setFirstName(required("Primer nombre", current == null ? null : current.getFirstName()));
        guardian.setMiddleName(optional("Segundo nombre", current == null ? null : current.getMiddleName()));
        guardian.setFirstLastName(required("Primer apellido", current == null ? null : current.getFirstLastName()));
        guardian.setSecondLastName(optional("Segundo apellido", current == null ? null : current.getSecondLastName()));
        guardian.setPrimaryPhone(required("Telefono principal", current == null ? null : current.getPrimaryPhone()));
        guardian.setSecondaryPhone(optional("Telefono secundario", current == null ? null : current.getSecondaryPhone()));
        guardian.setEmail(optional("Correo electronico", current == null ? null : current.getEmail()));
        guardian.setAddress(required("Direccion", current == null ? null : current.getAddress()));
        guardian.setOccupation(optional("Ocupacion", current == null ? null : current.getOccupation()));

        return guardian;
    }

    private void create() {
        Guardian guardian = readForm(null);
        showDetails(guardian);
        if (readYesNo("Guardar acudiente")) {
            printSuccess("Acudiente registrado. ID: " + controller.create(guardian));
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void update() {
        Guardian guardian = readForm(select());
        showDetails(guardian);
        if (readYesNo("Guardar cambios")) {
            controller.update(guardian);
            printSuccess("Acudiente actualizado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void findByDocument() {
        DocumentType type = choose("Tipo de documento", DocumentType.values(), DocumentType::getDbValue, null);
        controller.findByDocument(readRequiredText("Numero de documento: "), type)
                .ifPresentOrElse(GuardianView::showDetails, () -> printInfo("Acudiente no encontrado."));
    }

    static void showList(List<Guardian> guardians) {
        printTable(
                List.of("ID", "Nombre", "Tipo", "Documento", "Telefono", "Correo"),
                guardians.stream()
                        .map(guardian -> row(
                                guardian.getId(),
                                guardian.getFullName(),
                                guardian.getDocumentType().getDbValue(),
                                guardian.getDocumentNumber(),
                                guardian.getPrimaryPhone(),
                                guardian.getEmail()
                        ))
                        .toList()
        );
    }

    public static void showDetails(Guardian guardian) {
        printHeader("Detalle del acudiente");
        field("ID", guardian.getId());
        field("Nombre", guardian.getFullName());
        field("Tipo de documento", guardian.getDocumentType().getDbValue());
        field("Documento", guardian.getDocumentNumber());
        field("Telefono principal", guardian.getPrimaryPhone());
        field("Telefono secundario", guardian.getSecondaryPhone());
        field("Correo", guardian.getEmail());
        field("Direccion", guardian.getAddress());
        field("Ocupacion", guardian.getOccupation());
    }
}
