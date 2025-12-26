package dao;

import model.Feedback;

import java.util.List;

public interface FeedbackDao {
    Long create(Feedback feedback);
    List<Feedback> findAll();
    List<Feedback> findByFacility(Long facilityId);
    List<Feedback> findByUser(Long userId);
}


