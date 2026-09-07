package com.propscan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for price analysis and investment data.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceAnalysisDto {

    private String groupId;
    private Long propertyPrice;
    private String propertyPriceFormatted;

    private Long localityAveragePricePerSqft;
    private String localityAveragePricePerSqftFormatted;

    private Long propertyPricePerSqft;
    private String propertyPricePerSqftFormatted;

    private Long comparableAverage;
    private String comparableAverageFormatted;

    private Double differencePercentage;
    private String priceTrend;  // Below Average, At Average, Above Average

    // Investment metrics
    private int investmentScore;
    private String estimatedRentalYield;
    private Long estimatedMonthlyRent;
    private String demandIndicator;  // High, Moderate, Low

    // Disclaimer
    private String disclaimer;

    // Note: AI-powered field
    private boolean aiEnhanced;
}
