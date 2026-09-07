package com.propscan.repository;

import com.propscan.model.PropertyListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyListingRepository extends JpaRepository<PropertyListing, Long> {
    Optional<PropertyListing> findByExternalId(String externalId);
    List<PropertyListing> findByGroupId(String groupId);
    List<PropertyListing> findBySource(String source);
    List<PropertyListing> findByCityIgnoreCaseAndLocalityContainingIgnoreCase(String city, String locality);
}
