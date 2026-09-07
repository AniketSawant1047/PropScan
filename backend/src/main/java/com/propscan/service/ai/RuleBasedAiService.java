package com.propscan.service.ai;

import com.propscan.dto.AiScoreDto;
import com.propscan.dto.SearchRequestDto;
import com.propscan.model.PropertyGroup;
import com.propscan.model.PropertyListing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based AI Service implementation.
 * Always available, no external API dependency.
 * Uses deterministic scoring rules based on property attributes and user preferences.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "propscan.ai.provider", havingValue = "rule-based", matchIfMissing = true)
public class RuleBasedAiService implements AiService {

    // Locality → average price per sqft estimates (INR)
    private static final java.util.Map<String, Long> LOCALITY_PRICE_MAP = java.util.Map.ofEntries(
            java.util.Map.entry("wakad", 7900L),
            java.util.Map.entry("hinjewadi", 8200L),
            java.util.Map.entry("baner", 9500L),
            java.util.Map.entry("kharadi", 8800L),
            java.util.Map.entry("viman nagar", 9000L),
            java.util.Map.entry("kothrud", 10000L),
            java.util.Map.entry("aundh", 9800L),
            java.util.Map.entry("hadapsar", 7800L),
            java.util.Map.entry("balewadi", 9200L),
            java.util.Map.entry("pimple saudagar", 7500L)
    );

    @Override
    public SearchRequestDto parseNaturalLanguageQuery(String query) {
        if (query == null || query.isBlank()) return new SearchRequestDto();

        SearchRequestDto req = new SearchRequestDto();
        req.setNaturalLanguageQuery(query);

        String lower = query.toLowerCase();

        // Extract city
        if (lower.contains("pune")) req.setCity("Pune");
        else if (lower.contains("mumbai")) req.setCity("Mumbai");
        else if (lower.contains("bangalore") || lower.contains("bengaluru")) req.setCity("Bangalore");
        else req.setCity("Pune"); // default for demo

        // Extract locality
        for (String locality : List.of("wakad", "hinjewadi", "baner", "kharadi", "viman nagar",
                "kothrud", "aundh", "hadapsar", "balewadi", "pimple saudagar")) {
            if (lower.contains(locality)) {
                req.setLocality(capitalize(locality));
                break;
            }
        }

        // Extract BHK
        Matcher bhkMatcher = Pattern.compile("(\\d)\\s*bhk").matcher(lower);
        if (bhkMatcher.find()) {
            req.setBhk(Integer.parseInt(bhkMatcher.group(1)));
        }

        // Extract budget
        Matcher croreMatcher = Pattern.compile("(\\d+\\.?\\d*)\\s*(?:crore|cr)").matcher(lower);
        Matcher lakhMatcher = Pattern.compile("(\\d+\\.?\\d*)\\s*(?:lakh|lac|l)").matcher(lower);

        if (croreMatcher.find()) {
            req.setMaxBudget(Math.round(Double.parseDouble(croreMatcher.group(1)) * 10_000_000));
        } else if (lakhMatcher.find()) {
            req.setMaxBudget(Math.round(Double.parseDouble(lakhMatcher.group(1)) * 100_000));
        }

        // Extract commute
        Matcher commuteMatcher = Pattern.compile("(\\d+)\\s*(?:min|minute)").matcher(lower);
        if (commuteMatcher.find()) {
            req.setMaxCommuteMinutes(Integer.parseInt(commuteMatcher.group(1)));
        }

        // Parking
        if (lower.contains("parking")) req.setParkingRequired(true);

        // Property type
        if (lower.contains("villa")) req.setPropertyType("Villa");
        else if (lower.contains("apartment") || lower.contains("flat") || lower.contains("bhk")) {
            req.setPropertyType("Apartment");
        }

        log.info("Parsed NL query '{}' → city={}, locality={}, bhk={}, maxBudget={}",
                query, req.getCity(), req.getLocality(), req.getBhk(), req.getMaxBudget());

        return req;
    }

    @Override
    public AiScoreDto calculateScore(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request) {
        if (group == null) return defaultScore();

        int priceScore = calculatePriceScore(group, request);
        int locationScore = calculateLocationScore(group);
        int connectivityScore = calculateConnectivityScore(group);
        int sizeScore = calculateSizeScore(group, request);
        int amenitiesScore = calculateAmenitiesScore(listings);
        int userMatchScore = calculateUserMatchScore(group, listings, request);

        int overall = (int) Math.round(
                priceScore * 0.20 +
                locationScore * 0.20 +
                connectivityScore * 0.15 +
                sizeScore * 0.15 +
                amenitiesScore * 0.15 +
                userMatchScore * 0.15
        );

        // Clamp
        overall = Math.min(100, Math.max(0, overall));

        String grade = overall >= 90 ? "Excellent" :
                overall >= 75 ? "Good" :
                overall >= 60 ? "Fair" : "Poor";

        // Price analysis
        String locality = group.getCanonicalLocality();
        Long localityPpSqft = locality != null ? LOCALITY_PRICE_MAP.getOrDefault(locality.toLowerCase(), 8000L) : 8000L;
        Double propPpSqft = group.getCanonicalAreaSqft() != null && group.getBestPriceInr() != null
                ? group.getBestPriceInr() / group.getCanonicalAreaSqft() : null;
        Double priceDiff = propPpSqft != null ? ((propPpSqft - localityPpSqft) / localityPpSqft) * 100 : null;
        String priceVerdict = priceDiff == null ? "N/A" :
                priceDiff < -5 ? "Below Average" :
                priceDiff > 5 ? "Above Average" : "At Average";

        List<String> positives = buildPositives(group, listings, request, priceVerdict);
        List<String> verifyPoints = buildVerifyPoints(listings);

        return AiScoreDto.builder()
                .overallScore(overall)
                .grade(grade)
                .priceValue(priceScore)
                .location(locationScore)
                .connectivity(connectivityScore)
                .propertySize(sizeScore)
                .amenities(amenitiesScore)
                .userMatch(userMatchScore)
                .priceVerdict(priceVerdict)
                .priceVsLocalAverage(priceDiff)
                .parkingAvailable(listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getParking())))
                .gymAvailable(listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getGym())))
                .swimmingPoolAvailable(listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getSwimmingPool())))
                .positives(positives)
                .verifyPoints(verifyPoints)
                .aiGenerated(false)
                .build();
    }

    @Override
    public String generateRecommendation(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request, AiScoreDto score) {
        StringBuilder sb = new StringBuilder();

        sb.append("This property is ");
        if (score.getOverallScore() >= 90) {
            sb.append("an excellent match for your requirements. ");
        } else if (score.getOverallScore() >= 75) {
            sb.append("a good match for your requirements. ");
        } else {
            sb.append("a reasonable option to consider. ");
        }

        // Budget
        if (request != null && request.getMaxBudget() != null && group.getBestPriceInr() != null) {
            long saving = request.getMaxBudget() - group.getBestPriceInr();
            if (saving > 0) {
                sb.append("It is ₹").append(formatLakh(saving)).append(" below your maximum budget. ");
            }
        }

        // Source count
        if (group.getSources() != null && group.getSources().size() > 1) {
            sb.append("Listed on ").append(group.getSources().size())
              .append(" sources, with the best price available on ").append(group.getBestPriceSource()).append(". ");
        }

        // Parking
        boolean hasParking = listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getParking()));
        if (hasParking) sb.append("Parking is included. ");

        // Amenities
        boolean hasGym = listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getGym()));
        boolean hasPool = listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getSwimmingPool()));
        if (hasGym || hasPool) {
            sb.append("Amenities include ");
            if (hasGym) sb.append("a gym");
            if (hasGym && hasPool) sb.append(" and ");
            if (hasPool) sb.append("a swimming pool");
            sb.append(". ");
        }

        // Location insight
        sb.append("The location offers good connectivity to IT hubs and essential services.");

        return sb.toString();
    }

    @Override
    public boolean isAiEnhanced() {
        return false;
    }

    // --- Private Scoring Methods ---

    private int calculatePriceScore(PropertyGroup group, SearchRequestDto request) {
        if (group.getBestPriceInr() == null || group.getCanonicalAreaSqft() == null) return 70;

        double ppSqft = group.getBestPriceInr() / group.getCanonicalAreaSqft();
        String locality = group.getCanonicalLocality();
        long localityAvg = locality != null ? LOCALITY_PRICE_MAP.getOrDefault(locality.toLowerCase(), 8000L) : 8000L;

        double ratio = ppSqft / localityAvg;

        int score;
        if (ratio <= 0.85) score = 98;
        else if (ratio <= 0.90) score = 94;
        else if (ratio <= 0.95) score = 90;
        else if (ratio <= 1.00) score = 85;
        else if (ratio <= 1.05) score = 78;
        else if (ratio <= 1.10) score = 72;
        else score = 65;

        // Bonus if well under budget
        if (request != null && request.getMaxBudget() != null && group.getBestPriceInr() != null) {
            double budgetRatio = (double) group.getBestPriceInr() / request.getMaxBudget();
            if (budgetRatio < 0.85) score = Math.min(100, score + 5);
        }

        return score;
    }

    private int calculateLocationScore(PropertyGroup group) {
        String locality = group.getCanonicalLocality();
        if (locality == null) return 70;

        return switch (locality.toLowerCase()) {
            case "hinjewadi" -> 92;
            case "baner" -> 90;
            case "aundh" -> 89;
            case "viman nagar" -> 88;
            case "kothrud" -> 87;
            case "balewadi" -> 86;
            case "kharadi" -> 85;
            case "wakad" -> 88;
            case "pimple saudagar" -> 80;
            case "hadapsar" -> 78;
            default -> 75;
        };
    }

    private int calculateConnectivityScore(PropertyGroup group) {
        String locality = group.getCanonicalLocality();
        if (locality == null) return 70;

        return switch (locality.toLowerCase()) {
            case "hinjewadi" -> 94;
            case "wakad" -> 92;
            case "baner" -> 88;
            case "kharadi" -> 87;
            case "viman nagar" -> 86;
            case "aundh" -> 85;
            case "balewadi" -> 84;
            case "kothrud" -> 82;
            case "pimple saudagar" -> 80;
            case "hadapsar" -> 78;
            default -> 72;
        };
    }

    private int calculateSizeScore(PropertyGroup group, SearchRequestDto request) {
        if (group.getCanonicalAreaSqft() == null) return 75;

        double area = group.getCanonicalAreaSqft();
        Integer bhk = group.getCanonicalBhk();

        // Ideal area per BHK
        double ideal = switch (bhk != null ? bhk : 2) {
            case 1 -> 550;
            case 2 -> 1000;
            case 3 -> 1400;
            case 4 -> 1800;
            default -> 1000;
        };

        double ratio = area / ideal;
        if (ratio >= 1.15) return 98;
        if (ratio >= 1.05) return 93;
        if (ratio >= 0.95) return 88;
        if (ratio >= 0.85) return 82;
        if (ratio >= 0.75) return 74;
        return 65;
    }

    private int calculateAmenitiesScore(List<PropertyListing> listings) {
        if (listings == null || listings.isEmpty()) return 70;

        // Use first listing as representative
        PropertyListing ref = listings.get(0);
        int score = 60;

        if (Boolean.TRUE.equals(ref.getParking())) score += 8;
        if (Boolean.TRUE.equals(ref.getLift())) score += 5;
        if (Boolean.TRUE.equals(ref.getGym())) score += 8;
        if (Boolean.TRUE.equals(ref.getSwimmingPool())) score += 7;
        if (Boolean.TRUE.equals(ref.getSecurity())) score += 5;

        // Amenities list bonus
        List<String> amenities = ref.getAmenities();
        if (amenities != null) {
            score += Math.min(7, amenities.size());
        }

        return Math.min(100, score);
    }

    private int calculateUserMatchScore(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request) {
        if (request == null) return 80;

        int score = 100;

        // BHK match
        if (request.getBhk() != null && group.getCanonicalBhk() != null) {
            if (!request.getBhk().equals(group.getCanonicalBhk())) score -= 15;
        }

        // Budget match
        if (request.getMaxBudget() != null && group.getBestPriceInr() != null) {
            if (group.getBestPriceInr() > request.getMaxBudget()) score -= 20;
        }

        // Parking
        if (Boolean.TRUE.equals(request.getParkingRequired())) {
            boolean hasParking = listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getParking()));
            if (!hasParking) score -= 10;
        }

        // Locality match
        if (request.getLocality() != null && group.getCanonicalLocality() != null) {
            if (!group.getCanonicalLocality().toLowerCase().contains(request.getLocality().toLowerCase())) {
                score -= 5;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    private List<String> buildPositives(PropertyGroup group, List<PropertyListing> listings,
                                        SearchRequestDto request, String priceVerdict) {
        List<String> positives = new ArrayList<>();

        if ("Below Average".equals(priceVerdict)) {
            positives.add("Price is below locality average — strong value");
        }

        if (request != null && request.getMaxBudget() != null && group.getBestPriceInr() != null
                && group.getBestPriceInr() <= request.getMaxBudget()) {
            positives.add("Within your budget");
        }

        if (listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getParking()))) {
            positives.add("Parking included");
        }

        if (group.getSources() != null && group.getSources().size() > 1) {
            positives.add("Listed on " + group.getSources().size() + " platforms — wide availability");
        }

        if (listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getGym()))) {
            positives.add("Gym available in society");
        }

        String loc = group.getCanonicalLocality();
        if (loc != null && (loc.equalsIgnoreCase("Hinjewadi") || loc.equalsIgnoreCase("Wakad")
                || loc.equalsIgnoreCase("Baner"))) {
            positives.add("Good connectivity to IT hubs");
        }

        if (listings.stream().anyMatch(l -> Boolean.TRUE.equals(l.getReadyToMove()))) {
            positives.add("Ready to move in immediately");
        }

        return positives;
    }

    private List<String> buildVerifyPoints(List<PropertyListing> listings) {
        List<String> verifyPoints = new ArrayList<>();
        verifyPoints.add("Verify ownership documents before purchase");
        verifyPoints.add("Check for any pending maintenance charges");

        if (listings.stream().anyMatch(l -> l.getAmenities() != null && !l.getAmenities().isEmpty())) {
            verifyPoints.add("Confirm amenities are operational and maintained");
        }

        verifyPoints.add("Validate society rules and pet/rental policies");

        return verifyPoints;
    }

    private AiScoreDto defaultScore() {
        return AiScoreDto.builder()
                .overallScore(75)
                .grade("Good")
                .priceValue(75)
                .location(75)
                .connectivity(75)
                .propertySize(75)
                .amenities(75)
                .userMatch(75)
                .aiGenerated(false)
                .build();
    }

    private String formatLakh(long amount) {
        if (amount >= 10_000_000) {
            return String.format("%.2f Cr", amount / 10_000_000.0);
        }
        return String.format("%.1f L", amount / 100_000.0);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
