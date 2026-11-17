package model;

import java.time.LocalDate;
import model.enums.*;

public class Resource {
    private Integer id;
    private String code;
    private String name;
    private Integer categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String location;
    private ResourceStatus status;

    public Resource() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id=id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code=code; }

    public String getName() { return name; }
    public void setName(String name) { this.name=name; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId=categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }

    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate=acquisitionDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location=location; }

    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status=status; }
}
