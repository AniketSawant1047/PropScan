package com.propscan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO for location intelligence data including nearby places and commute info.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationIntelligenceDto {

    private String groupId;
    private String locality;
    private String city;
    private Double latitude;
    private Double longitude;

    private int locationScore;
    private int connectivityScore;
    private int publicTransportScore;
    private int schoolsScore;
    private int hospitalsScore;
    private int shoppingScore;
    private int officeAccessScore;
    private int lifestyleScore;

    private List<NearbyPlaceDto> nearbyPlaces;
    private List<CommuteDto> commutes;

    private String locationSummary;

    @Data
    @Builder
    public static class NearbyPlaceDto {
        private String name;
        private String category;   // metro, hospital, school, shopping, office, restaurant, gym, park, bank
        private String subcategory;
        private Double latitude;
        private Double longitude;
        private Double distanceKm;
        private Integer walkMinutes;
        private Integer driveMinutes;
        private String icon;
    }

    @Data
    @Builder
    public static class CommuteDto {
        private String destination;
        private String destinationType;  // office, school, hospital, other
        private Integer driveMinutes;
        private Integer transitMinutes;
        private Double distanceKm;
        private String route;
    }
}
