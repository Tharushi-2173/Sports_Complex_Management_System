package service.impl;

import dao.FacilityDao;
import model.Facility;
import model.FacilityStatus;
import service.FacilityService;

import java.util.List;
import java.util.Optional;

public class FacilityServiceImpl implements FacilityService {
    private final FacilityDao facilityDao;

    public FacilityServiceImpl(FacilityDao facilityDao) {
        this.facilityDao = facilityDao;
    }

    @Override
    public Long add(Facility facility) {
        validate(facility);
        return facilityDao.create(facility);
    }

    @Override
    public void update(Facility facility) {
        validate(facility);
        if (facility.getId() == null) throw new IllegalArgumentException("Facility id required");
        facilityDao.update(facility);
    }

    @Override
    public void remove(Long id) {
        facilityDao.delete(id);
    }

    @Override
    public Optional<Facility> get(Long id) { return facilityDao.findById(id); }

    @Override
    public List<Facility> listAll() { return facilityDao.findAll(); }

    @Override
    public List<Facility> listByStatus(FacilityStatus status) { return facilityDao.findByStatus(status); }

    private void validate(Facility f) {
        if (f.getName() == null || f.getName().trim().isEmpty()) throw new IllegalArgumentException("Name required");
        if (f.getHourlyRate() < 0) throw new IllegalArgumentException("Hourly rate must be >= 0");
        if (f.getStatus() == null) throw new IllegalArgumentException("Status required");
    }
}


