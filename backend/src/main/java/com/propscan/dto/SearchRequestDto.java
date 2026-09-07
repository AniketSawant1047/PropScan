package com.propscan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for search operations.
 */
@Data
public class SearchRequestDto {

    // Natural language query
    private String naturalLanguageQuery;

    // Structured search (parsed from NL or directly provided)
    private String city;
    private String locality;
    private String propertyType;
    private Integer bhk;
    private Long maxBudget;
    private Long minBudget;
    private Boolean parkingRequired;
    private Boolean gymRequired;
    private Boolean swimmingPoolRequired;
    private Integer maxCommuteMinutes;
    private String commuteDestination;
    private String furnishing;
    private Boolean readyToMove;

    // Sort & filter
    private String sortBy;   // price_asc, price_desc, score_desc, area_asc
    private Integer page;
    private Integer size;
}
