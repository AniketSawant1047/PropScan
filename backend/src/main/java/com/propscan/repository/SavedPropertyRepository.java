package com.propscan.repository;

import com.propscan.model.SavedProperty;
import com.propscan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPropertyRepository extends JpaRepository<SavedProperty, Long> {
    List<SavedProperty> findByUserOrderBySavedAtDesc(User user);
    Optional<SavedProperty> findByUserAndGroupId(User user, String groupId);
    boolean existsByUserAndGroupId(User user, String groupId);
    void deleteByUserAndGroupId(User user, String groupId);
}
