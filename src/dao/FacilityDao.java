package dao;

import model.Facility;
import model.FacilityStatus;

import java.util.List;
import java.util.Optional;

public interface FacilityDao {
    Long create(Facility facility);
    void update(Facility facility);
    void delete(Long id);
    Optional<Facility> findById(Long id);
    Optional<Facility> findByName(String name);
    List<Facility> findAll();
    List<Facility> findByStatus(FacilityStatus status);
}


