package com.propscan.service.location;

import com.propscan.dto.LocationIntelligenceDto;

/**
 * LocationService abstraction.
 * Supports geocoding, nearby places, commute estimates, and location scoring.
 * MockLocationService is the default implementation for the hackathon.
 */
public interface LocationService {

    /**
     * Get full location intelligence for a property.
     */
    LocationIntelligenceDto getLocationIntelligence(String groupId, Double latitude, Double longitude, String locality);

    /**
     * Calculate location score (0-100).
     */
    int calculateLocationScore(String locality);

    /**
     * Check if this is a mock or real implementation.
     */
    boolean isMock();
}
