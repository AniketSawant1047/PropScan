package com.propscan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for search results.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponseDto {

    private int totalListings;
    private int totalGroups;
    private int page;
    private int size;
    private int totalPages;

    private SearchRequestDto appliedFilters;
    private String interpretedQuery;

    private List<PropertyGroupDto> groups;

    // Source breakdown
    private List<SourceSummaryDto> sourceSummaries;

    private boolean fromCache;
    private long searchTimeMs;

    @Data
    @Builder
    public static class SourceSummaryDto {
        private String source;
        private String sourceDisplayName;
        private int listingsFound;
        private boolean available;
        private String error;
    }
}
