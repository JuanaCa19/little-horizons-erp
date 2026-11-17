package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.Club;

public interface ClubDAO {
    Optional<Club> findById(Integer id,Connection connection);
    int create(Club value);
    int create(Club value,Connection connection);
    Optional<Club> findById(Integer id);
    Optional<Club> findByName(String name);
    List<Club> findAll();
    void update(Club value, Connection connection);
}
