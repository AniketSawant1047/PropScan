package com.propscan.service.ai;

import com.propscan.dto.AiScoreDto;
import com.propscan.dto.SearchRequestDto;
import com.propscan.model.PropertyGroup;
import com.propscan.model.PropertyListing;

import java.util.List;

/**
 * AI Service abstraction.
 * Implementations: RuleBasedAiService (always available) and LlmAiService (optional).
 */
public interface AiService {

    /**
     * Parse a natural language query into structured search criteria.
     */
    SearchRequestDto parseNaturalLanguageQuery(String query);

    /**
     * Calculate the AI match score for a property group vs user requirements.
     */
    AiScoreDto calculateScore(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request);

    /**
     * Generate a recommendation explanation for a property.
     */
    String generateRecommendation(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request, AiScoreDto score);

    /**
     * Whether this is AI-enhanced or rule-based.
     */
    boolean isAiEnhanced();
}
