package com.propscan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for normalized property listing.
 * Used in API responses.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyListingDto {

    private Long id;
    private String externalId;
    private String source;
    private String sourceDisplayName;
    private String title;
    private String description;

    // Price
    private Long priceInr;
    private String priceFormatted;
    private String rawPrice;
    private Double pricePerSqft;

    // Property details
    private Double areaSqft;
    private String areaFormatted;
    private Integer bhk;
    private String propertyType;

    // Location
    private String locality;
    private String city;
    private String address;
    private String project;
    private String developer;

    // Property specs
    private String floor;
    private Integer totalFloors;
    private String facing;
    private String furnishing;
    private Boolean parking;
    private Boolean lift;
    private Boolean gym;
    private Boolean swimmingPool;
    private Boolean security;
    private List<String> amenities;

    // Media
    private List<String> images;
    private String primaryImage;

    // Links
    private String listingUrl;

    // Dates & agent
    private String postedDate;
    private Boolean readyToMove;
    private String possession;
    private String agentName;
    private String agentPhone;

    // Coordinates
    private Double latitude;
    private Double longitude;

    // Group info
    private String groupId;
}
