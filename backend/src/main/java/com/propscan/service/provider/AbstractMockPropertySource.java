package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.propscan.dto.SearchRequestDto;
import com.propscan.model.PropertyListing;
import com.propscan.util.PriceNormalizer;
import com.propscan.util.AreaNormalizer;
import com.propscan.util.BhkNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for mock JSON-based property source adapters.
 * Reads from classpath mock-data JSON files.
 */
@Slf4j
public abstract class AbstractMockPropertySource implements PropertySource {

    private final ObjectMapper objectMapper;
    private final String jsonFileName;
    private List<PropertyListing> cachedListings;

    protected AbstractMockPropertySource(ObjectMapper objectMapper, String jsonFileName) {
        this.objectMapper = objectMapper;
        this.jsonFileName = jsonFileName;
    }

    @Override
    public List<PropertyListing> fetchAllListings() {
        if (cachedListings != null) {
            return cachedListings;
        }

        try {
            ClassPathResource resource = new ClassPathResource("mock-data/" + jsonFileName);
            Map<String, Object> data = objectMapper.readValue(resource.getInputStream(), Map.class);

            List<Map<String, Object>> rawListings = (List<Map<String, Object>>) data.get("listings");
            String sourceDisplayName = (String) data.getOrDefault("sourceDisplayName", getSourceDisplayName());

            cachedListings = rawListings.stream()
                    .map(raw -> mapToListing(raw, sourceDisplayName))
                    .collect(Collectors.toList());

            log.info("Loaded {} listings from {}", cachedListings.size(), jsonFileName);
            return cachedListings;
        } catch (IOException e) {
            log.error("Failed to load mock data from {}: {}", jsonFileName, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<PropertyListing> fetchListings(SearchRequestDto request) {
        List<PropertyListing> all = fetchAllListings();

        return all.stream()
                .filter(listing -> matchesRequest(listing, request))
                .collect(Collectors.toList());
    }

    private boolean matchesRequest(PropertyListing listing, SearchRequestDto req) {
        if (req == null) return true;

        // City filter
        if (req.getCity() != null && !req.getCity().isBlank()) {
            if (!listing.getCity().equalsIgnoreCase(req.getCity())) return false;
        }

        // Locality filter
        if (req.getLocality() != null && !req.getLocality().isBlank()) {
            if (listing.getLocality() == null) return false;
            if (!listing.getLocality().toLowerCase().contains(req.getLocality().toLowerCase())) return false;
        }

        // BHK filter
        if (req.getBhk() != null && listing.getBhk() != null) {
            if (!listing.getBhk().equals(req.getBhk())) return false;
        }

        // Budget filter
        if (req.getMaxBudget() != null && listing.getPriceInr() != null) {
            if (listing.getPriceInr() > req.getMaxBudget()) return false;
        }
        if (req.getMinBudget() != null && listing.getPriceInr() != null) {
            if (listing.getPriceInr() < req.getMinBudget()) return false;
        }

        // Parking filter
        if (Boolean.TRUE.equals(req.getParkingRequired()) && !Boolean.TRUE.equals(listing.getParking())) {
            return false;
        }

        // Property type filter
        if (req.getPropertyType() != null && !req.getPropertyType().isBlank()) {
            if (listing.getPropertyType() == null) return false;
            if (!listing.getPropertyType().toLowerCase().contains(req.getPropertyType().toLowerCase())) return false;
        }

        return true;
    }

    private PropertyListing mapToListing(Map<String, Object> raw, String sourceDisplayName) {
        String rawPrice = getString(raw, "rawPrice");
        String rawArea = getString(raw, "rawArea");
        String rawBhk = getString(raw, "rawBhk");

        Long priceInr = PriceNormalizer.normalize(rawPrice);
        Double areaSqft = AreaNormalizer.normalize(rawArea);
        Integer bhk = BhkNormalizer.normalize(rawBhk);
        Double pricePerSqft = (priceInr != null && areaSqft != null && areaSqft > 0)
                ? priceInr / areaSqft : null;

        List<String> amenities = getStringList(raw, "amenities");
        List<String> images = getStringList(raw, "images");

        return PropertyListing.builder()
                .externalId(getString(raw, "externalId"))
                .source(getSourceId())
                .sourceDisplayName(sourceDisplayName)
                .title(getString(raw, "title"))
                .description(getString(raw, "description"))
                .rawPrice(rawPrice)
                .priceInr(priceInr)
                .rawArea(rawArea)
                .areaSqft(areaSqft)
                .rawBhk(rawBhk)
                .bhk(bhk)
                .propertyType(getString(raw, "propertyType"))
                .locality(getString(raw, "locality"))
                .city(getString(raw, "city"))
                .address(getString(raw, "address"))
                .project(getString(raw, "project"))
                .developer(getString(raw, "developer"))
                .floor(getString(raw, "floor"))
                .totalFloors(getInt(raw, "totalFloors"))
                .facing(getString(raw, "facing"))
                .furnishing(getString(raw, "furnishing"))
                .parking(getBool(raw, "parking"))
                .lift(getBool(raw, "lift"))
                .gym(getBool(raw, "gym"))
                .swimmingPool(getBool(raw, "swimmingPool"))
                .security(getBool(raw, "security"))
                .amenities(amenities)
                .images(images)
                .latitude(getDouble(raw, "latitude"))
                .longitude(getDouble(raw, "longitude"))
                .listingUrl(getString(raw, "listingUrl"))
                .postedDate(parseDate(getString(raw, "postedDate")))
                .readyToMove(getBool(raw, "readyToMove"))
                .possession(getString(raw, "possession"))
                .agentName(getString(raw, "agentName"))
                .agentPhone(getString(raw, "agentPhone"))
                .pricePerSqft(pricePerSqft)
                .build();
    }

    // --- Helpers ---

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Boolean getBool(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean b) return b;
        if (val instanceof String s) return Boolean.parseBoolean(s);
        return null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Double d) return d;
        if (val instanceof Integer i) return i.doubleValue();
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (Exception e) { return null; }
        }
        return null;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); } catch (Exception e) { return null; }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try { return LocalDate.parse(dateStr); } catch (Exception e) { return null; }
    }
}
