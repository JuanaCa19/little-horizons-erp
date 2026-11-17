package dao;

import model.*;
import java.util.List;
import java.util.Optional;

public interface GradeDAO {

    Optional<Grade> findById(Integer id);

    List<Grade> findAll();
}
