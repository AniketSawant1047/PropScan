package com.propscan.service.provider;

import com.propscan.dto.SearchRequestDto;
import com.propscan.model.PropertyListing;

import java.util.List;

/**
 * PropertySource adapter interface.
 * Each property source (99acres, MagicBricks, etc.) implements this.
 * Currently backed by mock JSON data. Replace with real API calls when authorized.
 */
public interface PropertySource {

    /**
     * Unique identifier for this source (e.g., "99acres", "magicbricks")
     */
    String getSourceId();

    /**
     * Human-readable display name for this source
     */
    String getSourceDisplayName();

    /**
     * Fetch listings matching the search criteria.
     * Returns normalized PropertyListing objects.
     */
    List<PropertyListing> fetchListings(SearchRequestDto request);

    /**
     * Fetch all listings from this source (for pre-loading).
     */
    List<PropertyListing> fetchAllListings();

    /**
     * Check if this source is currently available.
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * Source priority for ordering (lower = higher priority)
     */
    default int getPriority() {
        return 100;
    }
}
