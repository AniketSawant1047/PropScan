package com.propscan.service.search;

import com.propscan.dto.*;
import com.propscan.model.PropertyGroup;
import com.propscan.model.PropertyListing;
import com.propscan.service.ai.AiService;
import com.propscan.service.location.LocationService;
import com.propscan.service.provider.PropertySource;
import com.propscan.util.PriceNormalizer;
import com.propscan.util.AreaNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Core search orchestration service.
 * Coordinates multi-source searching, normalization, duplicate grouping, scoring.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertySearchService {

    private final List<PropertySource> propertySources;
    private final AiService aiService;
    private final LocationService locationService;

    private final AtomicLong groupIdCounter = new AtomicLong(1);

    public SearchResponseDto search(SearchRequestDto request) {
        long startTime = System.currentTimeMillis();

        // Parse natural language query if provided
        if (request.getNaturalLanguageQuery() != null && !request.getNaturalLanguageQuery().isBlank()) {
            SearchRequestDto parsed = aiService.parseNaturalLanguageQuery(request.getNaturalLanguageQuery());
            // Merge: parsed values fill in null fields
            if (request.getCity() == null) request.setCity(parsed.getCity());
            if (request.getLocality() == null) request.setLocality(parsed.getLocality());
            if (request.getBhk() == null) request.setBhk(parsed.getBhk());
            if (request.getMaxBudget() == null) request.setMaxBudget(parsed.getMaxBudget());
            if (request.getMinBudget() == null) request.setMinBudget(parsed.getMinBudget());
            if (request.getParkingRequired() == null) request.setParkingRequired(parsed.getParkingRequired());
            if (request.getMaxCommuteMinutes() == null) request.setMaxCommuteMinutes(parsed.getMaxCommuteMinutes());
        }

        // Fetch from all sources
        List<PropertyListing> allListings = new ArrayList<>();
        List<SearchResponseDto.SourceSummaryDto> sourceSummaries = new ArrayList<>();

        for (PropertySource source : propertySources) {
            try {
                if (!source.isAvailable()) {
                    sourceSummaries.add(SearchResponseDto.SourceSummaryDto.builder()
                            .source(source.getSourceId())
                            .sourceDisplayName(source.getSourceDisplayName())
                            .available(false)
                            .listingsFound(0)
                            .error(source.getSourceDisplayName() + " temporarily unavailable")
                            .build());
                    continue;
                }

                List<PropertyListing> listings = source.fetchListings(request);
                allListings.addAll(listings);

                sourceSummaries.add(SearchResponseDto.SourceSummaryDto.builder()
                        .source(source.getSourceId())
                        .sourceDisplayName(source.getSourceDisplayName())
                        .available(true)
                        .listingsFound(listings.size())
                        .build());

                log.debug("Source {} returned {} listings", source.getSourceId(), listings.size());
            } catch (Exception e) {
                log.warn("Source {} failed: {}", source.getSourceId(), e.getMessage());
                sourceSummaries.add(SearchResponseDto.SourceSummaryDto.builder()
                        .source(source.getSourceId())
                        .sourceDisplayName(source.getSourceDisplayName())
                        .available(false)
                        .listingsFound(0)
                        .error("Temporarily unavailable. Showing results from other sources.")
                        .build());
            }
        }

        log.info("Total listings fetched: {}", allListings.size());

        // Group duplicates
        List<PropertyGroup> groups = groupDuplicates(allListings);

        log.info("Grouped into {} property groups", groups.size());

        // Sort groups
        List<PropertyGroup> sortedGroups = sortGroups(groups, request);

        // Apply pagination
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        int from = page * size;
        int to = Math.min(from + size, sortedGroups.size());

        List<PropertyGroup> pagedGroups = from < sortedGroups.size()
                ? sortedGroups.subList(from, to)
                : new ArrayList<>();

        // Build listings map for scoring
        Map<String, List<PropertyListing>> listingsByGroup = allListings.stream()
                .filter(l -> l.getGroupId() != null)
                .collect(Collectors.groupingBy(PropertyListing::getGroupId));

        // Convert to DTOs with AI scores
        List<PropertyGroupDto> groupDtos = pagedGroups.stream()
                .map(g -> toGroupDto(g, listingsByGroup.getOrDefault(g.getGroupId(), new ArrayList<>()), request))
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - startTime;

        String interpretedQuery = buildInterpretedQuery(request);

        return SearchResponseDto.builder()
                .totalListings(allListings.size())
                .totalGroups(groups.size())
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) groups.size() / size))
                .groups(groupDtos)
                .sourceSummaries(sourceSummaries)
                .searchTimeMs(elapsed)
                .interpretedQuery(interpretedQuery)
                .appliedFilters(request)
                .build();
    }

    /**
     * Group listings that likely represent the same physical property.
     * Uses rule-based similarity scoring on: project, developer, location, BHK, area.
     */
    List<PropertyGroup> groupDuplicates(List<PropertyListing> listings) {
        List<PropertyGroup> groups = new ArrayList<>();
        boolean[] assigned = new boolean[listings.size()];

        for (int i = 0; i < listings.size(); i++) {
            if (assigned[i]) continue;

            PropertyListing anchor = listings.get(i);
            List<Integer> matchedIndices = new ArrayList<>();
            matchedIndices.add(i);

            for (int j = i + 1; j < listings.size(); j++) {
                if (assigned[j]) continue;
                double similarity = computeSimilarity(anchor, listings.get(j));
                if (similarity >= 0.75) {
                    matchedIndices.add(j);
                }
            }

            // Mark all matched as assigned
            for (int idx : matchedIndices) assigned[idx] = true;

            // Build the group
            List<PropertyListing> groupListings = matchedIndices.stream()
                    .map(listings::get)
                    .collect(Collectors.toList());

            PropertyGroup group = buildGroup(groupListings);
            groups.add(group);
        }

        return groups;
    }

    /**
     * Compute similarity score (0.0 to 1.0) between two listings.
     */
    private double computeSimilarity(PropertyListing a, PropertyListing b) {
        double score = 0.0;
        double total = 0.0;

        // Project name (highest weight)
        if (a.getProject() != null && b.getProject() != null) {
            double projectSim = stringSimilarity(a.getProject(), b.getProject());
            score += projectSim * 0.35;
            total += 0.35;
        }

        // Developer name
        if (a.getDeveloper() != null && b.getDeveloper() != null) {
            double devSim = stringSimilarity(a.getDeveloper(), b.getDeveloper());
            score += devSim * 0.15;
            total += 0.15;
        }

        // BHK
        if (a.getBhk() != null && b.getBhk() != null) {
            score += (a.getBhk().equals(b.getBhk()) ? 1.0 : 0.0) * 0.15;
            total += 0.15;
        }

        // Area (within 5%)
        if (a.getAreaSqft() != null && b.getAreaSqft() != null && a.getAreaSqft() > 0) {
            double areaDiff = Math.abs(a.getAreaSqft() - b.getAreaSqft()) / a.getAreaSqft();
            score += (areaDiff < 0.05 ? 1.0 : areaDiff < 0.10 ? 0.5 : 0.0) * 0.15;
            total += 0.15;
        }

        // Coordinates (within ~200m)
        if (a.getLatitude() != null && b.getLatitude() != null) {
            double dist = haversineDistance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
            score += (dist < 0.2 ? 1.0 : dist < 0.5 ? 0.5 : 0.0) * 0.20;
            total += 0.20;
        }

        if (total == 0) return 0.0;
        return score / total;
    }

    private double stringSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        String la = a.toLowerCase().trim();
        String lb = b.toLowerCase().trim();
        if (la.equals(lb)) return 1.0;
        if (la.contains(lb) || lb.contains(la)) return 0.85;

        // Jaccard on words
        Set<String> wa = new HashSet<>(Arrays.asList(la.split("\\s+")));
        Set<String> wb = new HashSet<>(Arrays.asList(lb.split("\\s+")));
        Set<String> intersection = new HashSet<>(wa);
        intersection.retainAll(wb);
        Set<String> union = new HashSet<>(wa);
        union.addAll(wb);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private PropertyGroup buildGroup(List<PropertyListing> groupListings) {
        String groupId = "PG" + String.format("%03d", groupIdCounter.getAndIncrement());

        // Mark all listings with this group
        groupListings.forEach(l -> l.setGroupId(groupId));

        // Find best (lowest) price
        PropertyListing bestListing = groupListings.stream()
                .filter(l -> l.getPriceInr() != null)
                .min(Comparator.comparingLong(PropertyListing::getPriceInr))
                .orElse(groupListings.get(0));

        PropertyListing highestListing = groupListings.stream()
                .filter(l -> l.getPriceInr() != null)
                .max(Comparator.comparingLong(PropertyListing::getPriceInr))
                .orElse(groupListings.get(0));

        // Use anchor (first) listing for canonical info
        PropertyListing anchor = groupListings.get(0);

        // Calculate match confidence based on how similar they are
        double confidence = groupListings.size() > 1
                ? Math.min(99.0, 70.0 + (computeSimilarity(groupListings.get(0), groupListings.get(groupListings.size() > 1 ? 1 : 0)) * 30))
                : 100.0;

        List<String> sources = groupListings.stream()
                .map(PropertyListing::getSource)
                .distinct()
                .collect(Collectors.toList());

        List<String> images = groupListings.stream()
                .flatMap(l -> l.getImages().stream())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        return PropertyGroup.builder()
                .groupId(groupId)
                .canonicalTitle(anchor.getTitle())
                .canonicalAddress(anchor.getAddress())
                .canonicalLocality(anchor.getLocality())
                .canonicalCity(anchor.getCity())
                .canonicalProject(anchor.getProject())
                .canonicalDeveloper(anchor.getDeveloper())
                .canonicalBhk(anchor.getBhk())
                .canonicalAreaSqft(anchor.getAreaSqft())
                .canonicalLatitude(anchor.getLatitude())
                .canonicalLongitude(anchor.getLongitude())
                .bestPriceInr(bestListing.getPriceInr())
                .bestPriceSource(bestListing.getSourceDisplayName())
                .highestPriceInr(highestListing.getPriceInr())
                .highestPriceSource(highestListing.getSourceDisplayName())
                .matchConfidence(Math.round(confidence * 10.0) / 10.0)
                .sources(sources)
                .images(images)
                .build();
    }

    private List<PropertyGroup> sortGroups(List<PropertyGroup> groups, SearchRequestDto request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "score_desc";

        return switch (sortBy) {
            case "price_asc" -> groups.stream()
                    .sorted(Comparator.comparingLong(g -> g.getBestPriceInr() != null ? g.getBestPriceInr() : Long.MAX_VALUE))
                    .collect(Collectors.toList());
            case "price_desc" -> groups.stream()
                    .sorted(Comparator.comparingLong((PropertyGroup g) -> g.getBestPriceInr() != null ? g.getBestPriceInr() : 0L).reversed())
                    .collect(Collectors.toList());
            case "area_asc" -> groups.stream()
                    .sorted(Comparator.comparingDouble(g -> g.getCanonicalAreaSqft() != null ? g.getCanonicalAreaSqft() : 0.0))
                    .collect(Collectors.toList());
            default -> // score_desc - sort by source count then confidence
                    groups.stream()
                            .sorted(Comparator.comparingInt((PropertyGroup g) -> g.getSources().size()).reversed()
                                    .thenComparingDouble((PropertyGroup g) -> g.getMatchConfidence() != null ? g.getMatchConfidence() : 0))
                            .collect(Collectors.toList());
        };
    }

    private PropertyGroupDto toGroupDto(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request) {
        // AI Score
        AiScoreDto score = aiService.calculateScore(group, listings, request);

        // Location score
        int locScore = locationService.calculateLocationScore(group.getCanonicalLocality());

        // Price calculations
        Long bestPrice = group.getBestPriceInr();
        Long highPrice = group.getHighestPriceInr();
        Long saving = (bestPrice != null && highPrice != null) ? highPrice - bestPrice : null;
        Double savingPct = (saving != null && highPrice != null && highPrice > 0)
                ? (saving.doubleValue() / highPrice) * 100 : null;

        Double ppSqft = (bestPrice != null && group.getCanonicalAreaSqft() != null && group.getCanonicalAreaSqft() > 0)
                ? bestPrice / group.getCanonicalAreaSqft() : null;

        // Build listing DTOs
        List<PropertyListingDto> listingDtos = listings.stream()
                .map(this::toListingDto)
                .collect(Collectors.toList());

        // Find best listing DTO
        PropertyListingDto bestListingDto = listings.stream()
                .filter(l -> l.getPriceInr() != null)
                .min(Comparator.comparingLong(PropertyListing::getPriceInr))
                .map(this::toListingDto)
                .orElse(listingDtos.isEmpty() ? null : listingDtos.get(0));

        return PropertyGroupDto.builder()
                .groupId(group.getGroupId())
                .canonicalTitle(group.getCanonicalTitle())
                .canonicalAddress(group.getCanonicalAddress())
                .canonicalLocality(group.getCanonicalLocality())
                .canonicalCity(group.getCanonicalCity())
                .canonicalProject(group.getCanonicalProject())
                .canonicalDeveloper(group.getCanonicalDeveloper())
                .canonicalBhk(group.getCanonicalBhk())
                .canonicalAreaSqft(group.getCanonicalAreaSqft())
                .areaFormatted(AreaNormalizer.format(group.getCanonicalAreaSqft()))
                .canonicalLatitude(group.getCanonicalLatitude())
                .canonicalLongitude(group.getCanonicalLongitude())
                .bestPriceInr(bestPrice)
                .bestPriceFormatted(PriceNormalizer.format(bestPrice))
                .bestPriceSource(group.getBestPriceSource())
                .highestPriceInr(highPrice)
                .highestPriceFormatted(PriceNormalizer.format(highPrice))
                .highestPriceSource(group.getHighestPriceSource())
                .potentialSavingInr(saving)
                .potentialSavingFormatted(saving != null ? PriceNormalizer.format(saving) : null)
                .savingPercentage(savingPct != null ? Math.round(savingPct * 10.0) / 10.0 : null)
                .bestPricePerSqft(ppSqft)
                .pricePerSqftFormatted(ppSqft != null ? String.format("₹%,.0f/sq.ft.", ppSqft) : null)
                .matchConfidence(group.getMatchConfidence())
                .sourceCount(group.getSources().size())
                .sources(group.getSources())
                .listings(listingDtos)
                .bestListing(bestListingDto)
                .images(group.getImages())
                .primaryImage(group.getImages() != null && !group.getImages().isEmpty() ? group.getImages().get(0) : null)
                .aiScore(score)
                .locationScore(locScore)
                .build();
    }

    private PropertyListingDto toListingDto(PropertyListing listing) {
        String primaryImage = listing.getImages() != null && !listing.getImages().isEmpty()
                ? listing.getImages().get(0) : null;

        return PropertyListingDto.builder()
                .id(listing.getId())
                .externalId(listing.getExternalId())
                .source(listing.getSource())
                .sourceDisplayName(listing.getSourceDisplayName())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .priceInr(listing.getPriceInr())
                .priceFormatted(PriceNormalizer.format(listing.getPriceInr()))
                .rawPrice(listing.getRawPrice())
                .pricePerSqft(listing.getPricePerSqft())
                .areaSqft(listing.getAreaSqft())
                .areaFormatted(AreaNormalizer.format(listing.getAreaSqft()))
                .bhk(listing.getBhk())
                .propertyType(listing.getPropertyType())
                .locality(listing.getLocality())
                .city(listing.getCity())
                .address(listing.getAddress())
                .project(listing.getProject())
                .developer(listing.getDeveloper())
                .floor(listing.getFloor())
                .totalFloors(listing.getTotalFloors())
                .facing(listing.getFacing())
                .furnishing(listing.getFurnishing())
                .parking(listing.getParking())
                .lift(listing.getLift())
                .gym(listing.getGym())
                .swimmingPool(listing.getSwimmingPool())
                .security(listing.getSecurity())
                .amenities(listing.getAmenities())
                .images(listing.getImages())
                .primaryImage(primaryImage)
                .listingUrl(listing.getListingUrl())
                .postedDate(listing.getPostedDate() != null ? listing.getPostedDate().toString() : null)
                .readyToMove(listing.getReadyToMove())
                .possession(listing.getPossession())
                .agentName(listing.getAgentName())
                .agentPhone(listing.getAgentPhone())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .groupId(listing.getGroupId())
                .build();
    }

    private String buildInterpretedQuery(SearchRequestDto req) {
        if (req == null) return "";
        List<String> parts = new ArrayList<>();
        if (req.getBhk() != null) parts.add(req.getBhk() + " BHK");
        if (req.getPropertyType() != null) parts.add(req.getPropertyType());
        if (req.getLocality() != null) parts.add("in " + req.getLocality());
        if (req.getCity() != null) parts.add(req.getCity());
        if (req.getMaxBudget() != null) parts.add("under " + PriceNormalizer.format(req.getMaxBudget()));
        if (Boolean.TRUE.equals(req.getParkingRequired())) parts.add("with parking");
        return String.join(" ", parts);
    }
}
