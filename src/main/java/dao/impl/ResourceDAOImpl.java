package dao.impl;

import dao.ResourceDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.ResourceStatus;

public class ResourceDAOImpl implements ResourceDAO {
    private ResourceCategory mapCategory(ResultSet rs) throws SQLException {
        ResourceCategory value=new ResourceCategory();value.setId(rs.getInt("id_categoria"));value.setName(rs.getString("nombre_categoria"));value.setDescription(rs.getString("descripcion"));return value;
    }
    private Resource mapResource(ResultSet rs) throws SQLException {
        Resource value=new Resource();value.setId(rs.getInt("id_recurso"));value.setCode(rs.getString("codigo_recurso"));value.setName(rs.getString("nombre_recurso"));
        value.setCategoryId(rs.getInt("id_categoria"));value.setDescription(rs.getString("descripcion"));value.setAcquisitionDate(rs.getObject("fecha_adquisicion",LocalDate.class));
        value.setLocation(rs.getString("ubicacion_fisica"));value.setStatus(ResourceStatus.fromDbValue(rs.getString("estado_recurso")));return value;
    }
    private void bind(PreparedStatement ps,Resource value) throws SQLException {
        ps.setString(1,value.getCode());ps.setString(2,value.getName());ps.setInt(3,value.getCategoryId());ps.setString(4,value.getDescription());
        ps.setObject(5,value.getAcquisitionDate(),Types.DATE);ps.setString(6,value.getLocation());ps.setString(7,value.getStatus().getDbValue());
    }
    @Override public int createCategory(ResourceCategory value,Connection c) {
        return JdbcSupport.insert(c,"INSERT INTO categorias_recurso (nombre_categoria,descripcion) VALUES (?,?)",ps -> { ps.setString(1,value.getName());ps.setString(2,value.getDescription()); });
    }
    @Override public Optional<ResourceCategory> findCategory(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM categorias_recurso WHERE id_categoria=?",ps -> ps.setInt(1,id),this::mapCategory).stream().findFirst();
    }
    @Override public Optional<ResourceCategory> findCategoryByName(String name,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM categorias_recurso WHERE nombre_categoria=?",ps -> ps.setString(1,name),this::mapCategory).stream().findFirst();
    }
    @Override public List<ResourceCategory> findCategories(Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM categorias_recurso ORDER BY nombre_categoria",ps -> {},this::mapCategory);
    }
    @Override public void updateCategory(ResourceCategory value,Connection c) {
        JdbcSupport.update(c,"UPDATE categorias_recurso SET nombre_categoria=?,descripcion=? WHERE id_categoria=?",ps -> { ps.setString(1,value.getName());ps.setString(2,value.getDescription());ps.setInt(3,value.getId()); });
    }
    @Override public int create(Resource value,Connection c) {
        return JdbcSupport.insert(c,"INSERT INTO recursos (codigo_recurso,nombre_recurso,id_categoria,descripcion,fecha_adquisicion,ubicacion_fisica,estado_recurso) VALUES (?,?,?,?,?,?,?)",ps -> bind(ps,value));
    }
    @Override public Optional<Resource> findById(Integer id,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM recursos WHERE id_recurso=? FOR UPDATE",ps -> ps.setInt(1,id),this::mapResource).stream().findFirst();
    }
    @Override public Optional<Resource> findByCode(String code,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM recursos WHERE codigo_recurso=?",ps -> ps.setString(1,code),this::mapResource).stream().findFirst();
    }
    @Override public List<Resource> findResources(String name,Integer categoryId,ResourceStatus status,Connection c) {
        String sql="SELECT * FROM recursos WHERE (? IS NULL OR nombre_recurso LIKE ? OR codigo_recurso LIKE ?) AND (? IS NULL OR id_categoria=?) AND (? IS NULL OR estado_recurso=?) ORDER BY nombre_recurso,id_recurso";
        return JdbcSupport.query(c,sql,ps -> {
            String pattern=name==null ? null : "%"+name+"%";ps.setString(1,name);ps.setString(2,pattern);ps.setString(3,pattern);
            ps.setObject(4,categoryId,Types.INTEGER);ps.setObject(5,categoryId,Types.INTEGER);
            ps.setString(6,status==null ? null : status.getDbValue());ps.setString(7,status==null ? null : status.getDbValue());
        },this::mapResource);
    }
    @Override public void update(Resource value,Connection c) {
        JdbcSupport.update(c,"UPDATE recursos SET codigo_recurso=?,nombre_recurso=?,id_categoria=?,descripcion=?,fecha_adquisicion=?,ubicacion_fisica=?,estado_recurso=? WHERE id_recurso=?",ps -> { bind(ps,value);ps.setInt(8,value.getId()); });
    }
    @Override public void updateStatus(Integer id,ResourceStatus status,Connection c) {
        JdbcSupport.update(c,"UPDATE recursos SET estado_recurso=? WHERE id_recurso=?",ps -> { ps.setString(1,status.getDbValue());ps.setInt(2,id); });
    }
}
