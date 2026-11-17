package controller;

import java.util.*;
import java.time.LocalDate;
import model.*;
import model.enums.*;
import service.ResourceService;

public class ResourceController {
    private final ResourceService service;
    public ResourceController() { this(new ResourceService()); }
    public ResourceController(ResourceService service) { this.service=Objects.requireNonNull(service); }

    public int saveCategory(ResourceCategory value) { return service.saveCategory(value); }

    public ResourceCategory getCategory(Integer id) { return service.getCategory(id); }

    public List<ResourceCategory> findCategories() { return service.findCategories(); }

    public int saveResource(Resource value) { return service.saveResource(value); }

    public Resource getResource(Integer id) { return service.getResource(id); }

    public Optional<Resource> findByCode(String code) { return service.findByCode(code); }

    public List<Resource> findResources(String query,Integer categoryId,ResourceStatus status) { return service.findResources(query,categoryId,status); }

    public void changeStatus(Integer id,ResourceStatus status) { service.changeStatus(id,status); }

    public int lend(ResourceLoan value) { return service.lend(value); }

    public ResourceLoan getLoan(BorrowerType type,Integer id) { return service.getLoan(type,id); }

    public void returnResource(BorrowerType type,Integer id,LocalDate date) { service.returnResource(type,id,date); }

    public List<ResourceLoan> findLoansByResource(Integer resourceId) { return service.findLoansByResource(resourceId); }

    public List<ResourceLoan> findLoansByBorrower(BorrowerType type,Integer borrowerId) { return service.findLoansByBorrower(type,borrowerId); }

    public List<ResourceLoan> findOverdueLoans(LocalDate date) { return service.findOverdueLoans(date); }
}
