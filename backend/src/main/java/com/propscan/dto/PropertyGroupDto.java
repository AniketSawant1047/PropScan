package com.propscan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO representing a group of property listings identified as the same physical property.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyGroupDto {

    private Long id;
    private String groupId;

    // Canonical (normalized) property info
    private String canonicalTitle;
    private String canonicalAddress;
    private String canonicalLocality;
    private String canonicalCity;
    private String canonicalProject;
    private String canonicalDeveloper;
    private Integer canonicalBhk;
    private Double canonicalAreaSqft;
    private String areaFormatted;
    private Double canonicalLatitude;
    private Double canonicalLongitude;

    // Price comparison
    private Long bestPriceInr;
    private String bestPriceFormatted;
    private String bestPriceSource;
    private Long highestPriceInr;
    private String highestPriceFormatted;
    private String highestPriceSource;
    private Long potentialSavingInr;
    private String potentialSavingFormatted;
    private Double savingPercentage;

    // Price per sqft
    private Double bestPricePerSqft;
    private String pricePerSqftFormatted;

    // Match info
    private Double matchConfidence;
    private int sourceCount;
    private List<String> sources;

    // Listings from each source
    private List<PropertyListingDto> listings;

    // Best listing (from best price source)
    private PropertyListingDto bestListing;

    // Images
    private List<String> images;
    private String primaryImage;

    // AI Score
    private AiScoreDto aiScore;

    // Location score
    private Integer locationScore;
}
