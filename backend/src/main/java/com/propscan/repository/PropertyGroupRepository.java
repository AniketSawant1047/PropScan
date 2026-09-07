package com.propscan.repository;

import com.propscan.model.PropertyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PropertyGroupRepository extends JpaRepository<PropertyGroup, Long> {
    Optional<PropertyGroup> findByGroupId(String groupId);
}
