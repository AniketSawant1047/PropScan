package com.propscan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for AI-generated property score breakdown.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiScoreDto {

    private int overallScore;
    private String grade;  // Excellent, Good, Fair, Poor

    // Score breakdown
    private int priceValue;
    private int location;
    private int connectivity;
    private int propertySize;
    private int amenities;
    private int userMatch;

    // Price analysis
    private String priceVerdict;  // Below Average, At Average, Above Average
    private Double priceVsLocalAverage;  // percentage difference

    private boolean parkingAvailable;
    private boolean gymAvailable;
    private boolean swimmingPoolAvailable;

    // Explanation
    private String summary;
    private java.util.List<String> positives;
    private java.util.List<String> verifyPoints;

    private boolean aiGenerated;  // false means rule-based
}
