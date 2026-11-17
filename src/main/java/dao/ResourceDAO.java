package dao;

import java.sql.Connection;
import java.util.*;
import model.*;
import model.enums.ResourceStatus;

public interface ResourceDAO {
    int createCategory(ResourceCategory value,Connection connection);
    Optional<ResourceCategory> findCategory(Integer id,Connection connection);
    Optional<ResourceCategory> findCategoryByName(String name,Connection connection);
    List<ResourceCategory> findCategories(Connection connection);
    void updateCategory(ResourceCategory value,Connection connection);
    int create(Resource value,Connection connection);
    Optional<Resource> findById(Integer id,Connection connection);
    Optional<Resource> findByCode(String code,Connection connection);
    List<Resource> findResources(String name,Integer categoryId,ResourceStatus status,Connection connection);
    void update(Resource value,Connection connection);
    void updateStatus(Integer id,ResourceStatus status,Connection connection);
}
