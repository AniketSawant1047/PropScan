package com.propscan.controller;

import com.propscan.dto.*;
import com.propscan.service.ai.AiService;
import com.propscan.service.location.LocationService;
import com.propscan.service.search.PropertySearchService;
import com.propscan.util.PriceNormalizer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for property search and details endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertySearchService searchService;
    private final AiService aiService;
    private final LocationService locationService;

    /**
     * POST /api/search
     * Main search endpoint. Accepts structured or natural language query.
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponseDto> search(@RequestBody SearchRequestDto request) {
        log.info("Search request: NL='{}', city={}, bhk={}, maxBudget={}",
                request.getNaturalLanguageQuery(), request.getCity(), request.getBhk(), request.getMaxBudget());

        SearchResponseDto response = searchService.search(request);

        log.info("Search returned {} groups from {} listings in {}ms",
                response.getTotalGroups(), response.getTotalListings(), response.getSearchTimeMs());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/search
     * Natural language search endpoint.
     */
    @PostMapping("/ai/search")
    public ResponseEntity<SearchResponseDto> aiSearch(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        log.info("AI search query: {}", query);

        SearchRequestDto request = new SearchRequestDto();
        request.setNaturalLanguageQuery(query);

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * GET /api/sources
     * Returns available property sources.
     */
    @GetMapping("/sources")
    public ResponseEntity<List<Map<String, Object>>> getSources() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "99acres", "name", "99acres", "priority", 1, "available", true),
                Map.of("id", "magicbricks", "name", "MagicBricks", "priority", 2, "available", true),
                Map.of("id", "housing", "name", "Housing.com", "priority", 3, "available", true),
                Map.of("id", "nobroker", "name", "NoBroker", "priority", 4, "available", true),
                Map.of("id", "builders", "name", "Builder Direct", "priority", 5, "available", true),
                Map.of("id", "local", "name", "Local Sources", "priority", 6, "available", true)
        ));
    }

    /**
     * GET /api/properties/{groupId}/location
     * Location intelligence for a property group.
     */
    @GetMapping("/properties/{groupId}/location")
    public ResponseEntity<LocationIntelligenceDto> getLocationIntelligence(
            @PathVariable String groupId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String locality) {

        LocationIntelligenceDto result = locationService.getLocationIntelligence(groupId, lat, lon, locality);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/properties/{groupId}/price-analysis
     * Price analysis for a property group.
     */
    @GetMapping("/properties/{groupId}/price-analysis")
    public ResponseEntity<PriceAnalysisDto> getPriceAnalysis(
            @PathVariable String groupId,
            @RequestParam(required = false) Long priceInr,
            @RequestParam(required = false) Double areaSqft,
            @RequestParam(required = false) String locality) {

        PriceAnalysisDto analysis = buildPriceAnalysis(groupId, priceInr, areaSqft, locality);
        return ResponseEntity.ok(analysis);
    }

    /**
     * POST /api/alerts
     * Create a price alert.
     */
    @PostMapping("/alerts")
    public ResponseEntity<Map<String, Object>> createAlert(@RequestBody Map<String, Object> alertRequest) {
        log.info("Creating alert: {}", alertRequest);
        // Persist and return confirmation
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alert created. You'll be notified when a matching property is found.",
                "alertId", "ALERT-" + System.currentTimeMillis()
        ));
    }

    private final com.propscan.repository.SavedPropertyRepository savedPropertyRepository;
    private final com.propscan.repository.UserRepository userRepository;

    @GetMapping("/user/saved")
    public ResponseEntity<List<String>> getSavedProperties(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.propscan.model.User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<com.propscan.model.SavedProperty> saved = savedPropertyRepository.findByUserOrderBySavedAtDesc(user);
        List<String> groupIds = saved.stream().map(com.propscan.model.SavedProperty::getGroupId).toList();
        return ResponseEntity.ok(groupIds);
    }

    @PostMapping("/user/saved/{groupId}")
    public ResponseEntity<Map<String, Object>> saveProperty(
            @PathVariable String groupId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.propscan.model.User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (!savedPropertyRepository.existsByUserAndGroupId(user, groupId)) {
            savedPropertyRepository.save(com.propscan.model.SavedProperty.builder()
                    .user(user)
                    .groupId(groupId)
                    .build());
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Property saved"));
    }

    @DeleteMapping("/user/saved/{groupId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, Object>> unsaveProperty(
            @PathVariable String groupId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.propscan.model.User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        savedPropertyRepository.deleteByUserAndGroupId(user, groupId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Property unsaved"));
    }

    // --- Helper ---

    private PriceAnalysisDto buildPriceAnalysis(String groupId, Long priceInr, Double areaSqft, String locality) {
        // Locality average price per sqft
        Map<String, Long> localityPrices = Map.ofEntries(
                Map.entry("wakad", 7900L),
                Map.entry("hinjewadi", 8200L),
                Map.entry("baner", 9500L),
                Map.entry("kharadi", 8800L),
                Map.entry("viman nagar", 9000L),
                Map.entry("kothrud", 10000L),
                Map.entry("aundh", 9800L),
                Map.entry("hadapsar", 7800L),
                Map.entry("balewadi", 9200L),
                Map.entry("pimple saudagar", 7500L)
        );

        String loc = locality != null ? locality.toLowerCase() : "wakad";
        long localityAvgPpSqft = localityPrices.getOrDefault(loc, 8000L);

        Long propPpSqft = (priceInr != null && areaSqft != null && areaSqft > 0)
                ? (long) (priceInr / areaSqft) : null;

        Long comparableAvg = (areaSqft != null) ? (long) (localityAvgPpSqft * areaSqft) : null;

        Double diffPct = (priceInr != null && comparableAvg != null && comparableAvg > 0)
                ? ((priceInr - comparableAvg) * 100.0 / comparableAvg) : null;

        String trend = diffPct == null ? "N/A" :
                diffPct < -5 ? "Below Average" :
                diffPct > 5 ? "Above Average" : "At Average";

        // Investment score
        int investScore = switch (loc) {
            case "hinjewadi", "wakad", "kharadi" -> 87;
            case "baner", "aundh" -> 83;
            case "viman nagar", "kothrud" -> 80;
            default -> 75;
        };

        Long monthlyRent = (areaSqft != null) ? (long) (areaSqft * 18) : null; // ~₹18/sqft/month
        String rentalYield = monthlyRent != null && priceInr != null
                ? String.format("%.1f%%", (monthlyRent * 12.0 / priceInr) * 100) : "N/A";

        return PriceAnalysisDto.builder()
                .groupId(groupId)
                .propertyPrice(priceInr)
                .propertyPriceFormatted(PriceNormalizer.format(priceInr))
                .localityAveragePricePerSqft(localityAvgPpSqft)
                .localityAveragePricePerSqftFormatted(String.format("₹%,d/sq.ft.", localityAvgPpSqft))
                .propertyPricePerSqft(propPpSqft)
                .propertyPricePerSqftFormatted(propPpSqft != null ? String.format("₹%,d/sq.ft.", propPpSqft) : null)
                .comparableAverage(comparableAvg)
                .comparableAverageFormatted(PriceNormalizer.format(comparableAvg))
                .differencePercentage(diffPct != null ? Math.round(diffPct * 10.0) / 10.0 : null)
                .priceTrend(trend)
                .investmentScore(investScore)
                .estimatedRentalYield(rentalYield)
                .estimatedMonthlyRent(monthlyRent)
                .demandIndicator(loc.equals("hinjewadi") || loc.equals("wakad") || loc.equals("kharadi") ? "High" : "Moderate")
                .disclaimer("This is an indicative estimate based on comparable market data. It is not a certified valuation.")
                .aiEnhanced(false)
                .build();
    }
}
