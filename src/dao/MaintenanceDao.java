package dao;

import model.MaintenanceRequest;
import model.MaintenanceStatus;

import java.util.List;
import java.util.Optional;

public interface MaintenanceDao {
    Long create(MaintenanceRequest request);
    void update(MaintenanceRequest request);
    Optional<MaintenanceRequest> findById(Long id);
    List<MaintenanceRequest> findAll();
    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);
}


