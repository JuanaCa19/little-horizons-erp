package dao;

import model.*;
import model.enums.*;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface GuardianDAO {

    int create(Guardian guardian);

    int create(Guardian guardian, Connection connection);

    Optional<Guardian> findById(Integer id);

    Optional<Guardian> findByDocument(String documentNumber, DocumentType documentType);

    boolean existsByDocument(DocumentType documentType, String documentNumber);

    List<Guardian> findAll();

    List<Guardian> searchByName(String name);

    void update(Guardian guardian);

}
