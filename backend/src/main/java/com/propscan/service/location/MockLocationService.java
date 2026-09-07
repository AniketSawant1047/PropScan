package com.propscan.service.location;

import com.propscan.dto.LocationIntelligenceDto;
import com.propscan.dto.LocationIntelligenceDto.NearbyPlaceDto;
import com.propscan.dto.LocationIntelligenceDto.CommuteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Mock implementation of LocationService.
 * Provides realistic location data for Pune localities.
 * Replace with Google Maps / HERE Maps API for production.
 */
@Slf4j
@Service
public class MockLocationService implements LocationService {

    @Override
    public LocationIntelligenceDto getLocationIntelligence(String groupId, Double lat, Double lon, String locality) {
        String loc = locality != null ? locality.toLowerCase() : "wakad";

        int locScore = calculateLocationScore(loc);
        int connectivityScore = getConnectivityScore(loc);
        int publicTransportScore = getPublicTransportScore(loc);
        int schoolsScore = 85;
        int hospitalsScore = 88;
        int shoppingScore = 87;
        int officeScore = getOfficeAccessScore(loc);
        int lifestyleScore = 84;

        List<NearbyPlaceDto> nearbyPlaces = getNearbyPlaces(loc, lat, lon);
        List<CommuteDto> commutes = getCommutes(loc);

        return LocationIntelligenceDto.builder()
                .groupId(groupId)
                .locality(locality)
                .city("Pune")
                .latitude(lat)
                .longitude(lon)
                .locationScore(locScore)
                .connectivityScore(connectivityScore)
                .publicTransportScore(publicTransportScore)
                .schoolsScore(schoolsScore)
                .hospitalsScore(hospitalsScore)
                .shoppingScore(shoppingScore)
                .officeAccessScore(officeScore)
                .lifestyleScore(lifestyleScore)
                .nearbyPlaces(nearbyPlaces)
                .commutes(commutes)
                .locationSummary(generateLocationSummary(loc, locScore))
                .build();
    }

    @Override
    public int calculateLocationScore(String locality) {
        if (locality == null) return 75;
        return switch (locality.toLowerCase()) {
            case "hinjewadi" -> 92;
            case "wakad"     -> 90;
            case "baner"     -> 89;
            case "kharadi"   -> 87;
            case "viman nagar" -> 88;
            case "aundh"     -> 86;
            case "balewadi"  -> 84;
            case "kothrud"   -> 83;
            case "pimple saudagar" -> 80;
            case "hadapsar"  -> 78;
            default          -> 75;
        };
    }

    @Override
    public boolean isMock() { return true; }

    // --- Private helpers ---

    private int getConnectivityScore(String loc) {
        return switch (loc) {
            case "hinjewadi" -> 94;
            case "wakad"     -> 92;
            case "baner"     -> 88;
            case "kharadi"   -> 87;
            case "viman nagar" -> 90;
            default          -> 80;
        };
    }

    private int getPublicTransportScore(String loc) {
        return switch (loc) {
            case "hinjewadi" -> 80;
            case "wakad"     -> 82;
            case "viman nagar" -> 88;
            case "kharadi"   -> 85;
            default          -> 78;
        };
    }

    private int getOfficeAccessScore(String loc) {
        return switch (loc) {
            case "hinjewadi" -> 98;
            case "wakad"     -> 95;
            case "kharadi"   -> 94;
            case "viman nagar" -> 90;
            case "baner"     -> 85;
            default          -> 78;
        };
    }

    private List<NearbyPlaceDto> getNearbyPlaces(String loc, Double lat, Double lon) {
        if (lat == null) lat = 18.601;
        if (lon == null) lon = 73.754;

        List<NearbyPlaceDto> places = new ArrayList<>();

        // Add places based on locality
        places.addAll(getMetroStations(loc, lat, lon));
        places.addAll(getHospitals(loc, lat, lon));
        places.addAll(getSchools(loc, lat, lon));
        places.addAll(getShopping(loc, lat, lon));
        places.addAll(getOffices(loc, lat, lon));
        places.addAll(getRestaurants(loc, lat, lon));

        return places;
    }

    private List<NearbyPlaceDto> getMetroStations(String loc, double lat, double lon) {
        return List.of(
                NearbyPlaceDto.builder()
                        .name("Wakad Metro Station")
                        .category("metro")
                        .latitude(lat + 0.005)
                        .longitude(lon + 0.008)
                        .distanceKm(1.2)
                        .walkMinutes(15)
                        .driveMinutes(4)
                        .icon("metro")
                        .build(),
                NearbyPlaceDto.builder()
                        .name("Hinjewadi Metro Hub")
                        .category("metro")
                        .latitude(lat - 0.012)
                        .longitude(lon - 0.015)
                        .distanceKm(2.8)
                        .walkMinutes(35)
                        .driveMinutes(8)
                        .icon("metro")
                        .build()
        );
    }

    private List<NearbyPlaceDto> getHospitals(String loc, double lat, double lon) {
        return List.of(
                NearbyPlaceDto.builder()
                        .name("Medipoint Hospital")
                        .category("hospital")
                        .latitude(lat + 0.010)
                        .longitude(lon + 0.005)
                        .distanceKm(1.8)
                        .walkMinutes(22)
                        .driveMinutes(6)
                        .icon("hospital")
                        .build(),
                NearbyPlaceDto.builder()
                        .name("Sahyadri Hospital Wakad")
                        .category("hospital")
                        .latitude(lat - 0.008)
                        .longitude(lon + 0.012)
                        .distanceKm(2.3)
                        .walkMinutes(28)
                        .driveMinutes(7)
                        .icon("hospital")
                        .build()
        );
    }

    private List<NearbyPlaceDto> getSchools(String loc, double lat, double lon) {
        return List.of(
                NearbyPlaceDto.builder()
                        .name("Indira National School")
                        .category("school")
                        .latitude(lat + 0.006)
                        .longitude(lon - 0.008)
                        .distanceKm(1.1)
                        .walkMinutes(14)
                        .driveMinutes(4)
                        .icon("school")
                        .build(),
                NearbyPlaceDto.builder()
                        .name("DY Patil International School")
                        .category("school")
                        .latitude(lat + 0.015)
                        .longitude(lon + 0.020)
                        .distanceKm(3.2)
                        .walkMinutes(40)
                        .driveMinutes(10)
                        .icon("school")
                        .build()
        );
    }

    private List<NearbyPlaceDto> getShopping(String loc, double lat, double lon) {
        return List.of(
                NearbyPlaceDto.builder()
                        .name("Xion Mall")
                        .category("shopping")
                        .latitude(lat - 0.010)
                        .longitude(lon - 0.005)
                        .distanceKm(1.5)
                        .walkMinutes(18)
                        .driveMinutes(5)
                        .icon("shopping")
                        .build(),
                NearbyPlaceDto.builder()
                        .name("D-Mart Wakad")
                        .category("shopping")
                        .latitude(lat + 0.008)
                        .longitude(lon - 0.010)
                        .distanceKm(0.9)
                        .walkMinutes(11)
                        .driveMinutes(3)
                        .icon("shopping")
                        .build()
        );
    }

    private List<NearbyPlaceDto> getOffices(String loc, double lat, double lon) {
        return List.of(
                NearbyPlaceDto.builder()
                        .name("Hinjewadi IT Park Phase 1")
                        .category("office")
                        .latitude(lat - 0.012)
                        .longitude(lon - 0.018)
                        .distanceKm(2.5)
                        .walkMinutes(30)
                        .driveMinutes(8)
                        .icon("office")
                        .build(),
                NearbyPlaceDto.builder()
                        .name("Hinjewadi Phase 2")
                        .category("office")
                        .latitude(lat - 0.015)
                        .longitude(lon - 0.022)
                        .distanceKm(3.8)
                        .driveMinutes(12)
                        .icon("office")
                        .build()
        );
    }

    private List<NearbyPlaceDto> getRestaurants(String loc, double lat, double lon) {
        return List.of(
                NearbyPlaceDto.builder()
                        .name("Hotel Woodland")
                        .category("restaurant")
                        .latitude(lat + 0.003)
                        .longitude(lon + 0.003)
                        .distanceKm(0.5)
                        .walkMinutes(6)
                        .driveMinutes(2)
                        .icon("restaurant")
                        .build(),
                NearbyPlaceDto.builder()
                        .name("Café Goodluck")
                        .category("restaurant")
                        .latitude(lat - 0.003)
                        .longitude(lon + 0.005)
                        .distanceKm(0.7)
                        .walkMinutes(9)
                        .driveMinutes(3)
                        .icon("restaurant")
                        .build()
        );
    }

    private List<CommuteDto> getCommutes(String loc) {
        return switch (loc) {
            case "wakad" -> List.of(
                    CommuteDto.builder().destination("Hinjewadi Phase 2 IT Park").destinationType("office").driveMinutes(12).transitMinutes(20).distanceKm(4.2).route("Via Dange Chowk").build(),
                    CommuteDto.builder().destination("Baner").destinationType("other").driveMinutes(10).transitMinutes(18).distanceKm(3.5).route("Via Baner Road").build(),
                    CommuteDto.builder().destination("Kothrud").destinationType("other").driveMinutes(22).transitMinutes(35).distanceKm(8.0).route("Via Pashan Road").build()
            );
            case "hinjewadi" -> List.of(
                    CommuteDto.builder().destination("Hinjewadi Phase 1").destinationType("office").driveMinutes(5).transitMinutes(10).distanceKm(1.5).route("Internal road").build(),
                    CommuteDto.builder().destination("Baner").destinationType("other").driveMinutes(15).transitMinutes(25).distanceKm(6.0).route("Via Baner Road").build(),
                    CommuteDto.builder().destination("Wakad").destinationType("other").driveMinutes(10).transitMinutes(18).distanceKm(3.8).route("Via Dange Chowk").build()
            );
            default -> List.of(
                    CommuteDto.builder().destination("Pune CBD").destinationType("other").driveMinutes(25).transitMinutes(40).distanceKm(10.0).route("Via NH48").build(),
                    CommuteDto.builder().destination("Hinjewadi IT Park").destinationType("office").driveMinutes(20).transitMinutes(35).distanceKm(8.5).route("Via Baner Road").build()
            );
        };
    }

    private String generateLocationSummary(String loc, int score) {
        String capitalLoc = loc.isEmpty() ? "This area" :
                Character.toUpperCase(loc.charAt(0)) + loc.substring(1);
        return capitalLoc + " is a well-connected locality in Pune with a location score of " + score + "/100. " +
                "It offers good access to IT hubs, quality schools, hospitals, and essential services. " +
                "The area has seen consistent demand from IT professionals and families.";
    }
}
