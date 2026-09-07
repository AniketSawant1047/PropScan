package com.propscan.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes area strings to sq ft (Double).
 *
 * Handles:
 * - "1,060 sq.ft." → 1060.0
 * - "975 sq ft" → 975.0
 * - "1550 sqft" → 1550.0
 * - "144 sq.m." → 1550.0 (converted from sq meters)
 */
@Slf4j
@UtilityClass
public class AreaNormalizer {

    private static final Pattern SQFT_PATTERN = Pattern.compile(
            "([0-9,]+\\.?[0-9]*)\\s*(?:sq\\.?\\s*ft\\.?|sqft|square\\s*feet|sft)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQM_PATTERN = Pattern.compile(
            "([0-9,]+\\.?[0-9]*)\\s*(?:sq\\.?\\s*m\\.?|sqm|square\\s*met(?:er|re)s?)",
            Pattern.CASE_INSENSITIVE
    );

    public static Double normalize(String rawArea) {
        if (rawArea == null || rawArea.isBlank()) return null;

        String cleaned = rawArea.trim();

        Matcher sqftMatcher = SQFT_PATTERN.matcher(cleaned);
        if (sqftMatcher.find()) {
            try {
                return Double.parseDouble(sqftMatcher.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                log.debug("Failed to parse sq ft: {}", rawArea);
            }
        }

        Matcher sqmMatcher = SQM_PATTERN.matcher(cleaned);
        if (sqmMatcher.find()) {
            try {
                double sqm = Double.parseDouble(sqmMatcher.group(1).replace(",", ""));
                return sqm * 10.7639; // convert sq meters to sq ft
            } catch (NumberFormatException e) {
                log.debug("Failed to parse sq m: {}", rawArea);
            }
        }

        // Try plain number
        try {
            String digits = cleaned.replaceAll("[^0-9.]", "");
            if (!digits.isBlank()) {
                return Double.parseDouble(digits);
            }
        } catch (NumberFormatException e) {
            log.debug("Failed to parse plain area: {}", rawArea);
        }

        return null;
    }

    public static String format(Double areaSqft) {
        if (areaSqft == null) return "N/A";
        if (areaSqft == Math.floor(areaSqft)) {
            return String.format("%,d sq.ft.", (long) Math.floor(areaSqft));
        }
        return String.format("%.0f sq.ft.", areaSqft);
    }
}
