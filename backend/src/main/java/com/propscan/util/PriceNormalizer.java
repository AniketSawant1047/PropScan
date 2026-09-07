package com.propscan.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes price strings from various sources to a consistent Long value in INR (paise not included).
 *
 * Handles formats like:
 * - "78 Lakh" → 7800000
 * - "0.78 Cr" → 7800000
 * - "1.25 Crore" → 12500000
 * - "7800000" → 7800000
 * - "₹76.5 Lakh" → 7650000
 */
@Slf4j
@UtilityClass
public class PriceNormalizer {

    private static final Pattern LAKH_PATTERN = Pattern.compile(
            "[₹\\s]*([0-9]+\\.?[0-9]*)\\s*(?:Lakh|Lakhs|L|lakh|lakhs|lac|Lac)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CRORE_PATTERN = Pattern.compile(
            "[₹\\s]*([0-9]+\\.?[0-9]*)\\s*(?:Crore|Crores|Cr|crore|crores|cr)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PLAIN_NUMBER = Pattern.compile("^[₹\\s]*([0-9,]+)\\s*$");

    /**
     * Normalize a raw price string to INR long value.
     * Returns null if parsing fails.
     */
    public static Long normalize(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) return null;

        String cleaned = rawPrice.trim();

        // Try lakh pattern first
        Matcher lakhMatcher = LAKH_PATTERN.matcher(cleaned);
        if (lakhMatcher.find()) {
            try {
                double value = Double.parseDouble(lakhMatcher.group(1));
                return Math.round(value * 100_000);
            } catch (NumberFormatException e) {
                log.debug("Failed to parse lakh value from: {}", rawPrice);
            }
        }

        // Try crore pattern
        Matcher croreMatcher = CRORE_PATTERN.matcher(cleaned);
        if (croreMatcher.find()) {
            try {
                double value = Double.parseDouble(croreMatcher.group(1));
                return Math.round(value * 10_000_000);
            } catch (NumberFormatException e) {
                log.debug("Failed to parse crore value from: {}", rawPrice);
            }
        }

        // Try plain number
        Matcher plainMatcher = PLAIN_NUMBER.matcher(cleaned);
        if (plainMatcher.find()) {
            try {
                String digits = plainMatcher.group(1).replace(",", "");
                return Long.parseLong(digits);
            } catch (NumberFormatException e) {
                log.debug("Failed to parse plain number from: {}", rawPrice);
            }
        }

        log.warn("Could not normalize price: {}", rawPrice);
        return null;
    }

    /**
     * Format a price in INR to a human-readable string.
     * e.g., 7800000 → "₹78L", 12500000 → "₹1.25Cr"
     */
    public static String format(Long priceInr) {
        if (priceInr == null) return "N/A";

        if (priceInr >= 10_000_000) {
            double crore = priceInr / 10_000_000.0;
            if (crore == Math.floor(crore)) {
                return "₹" + (long) crore + " Cr";
            }
            return "₹" + String.format("%.2f", crore).replaceAll("0+$", "").replaceAll("\\.$", "") + " Cr";
        } else if (priceInr >= 100_000) {
            double lakh = priceInr / 100_000.0;
            if (lakh == Math.floor(lakh)) {
                return "₹" + (long) lakh + "L";
            }
            return "₹" + String.format("%.1f", lakh).replaceAll("0+$", "").replaceAll("\\.$", "") + "L";
        } else {
            return "₹" + String.format("%,d", priceInr);
        }
    }
}
