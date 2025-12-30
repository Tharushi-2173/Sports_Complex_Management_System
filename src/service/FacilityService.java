package service;

import model.Facility;
import model.FacilityStatus;

import java.util.List;
import java.util.Optional;

public interface FacilityService {
    Long add(Facility facility);
    void update(Facility facility);
    void remove(Long id);
    Optional<Facility> get(Long id);
    List<Facility> listAll();
    List<Facility> listByStatus(FacilityStatus status);
}


