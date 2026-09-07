package com.propscan.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes BHK strings to an integer.
 *
 * Handles:
 * - "2 BHK" → 2
 * - "2BHK" → 2
 * - "3BHK" → 3
 * - "1 RK" → 1
 * - "Studio" → 1
 */
@Slf4j
@UtilityClass
public class BhkNormalizer {

    private static final Pattern BHK_PATTERN = Pattern.compile(
            "([0-9]+)\\s*(?:BHK|bhk|Bhk|RK|rk|BR|br)",
            Pattern.CASE_INSENSITIVE
    );

    public static Integer normalize(String rawBhk) {
        if (rawBhk == null || rawBhk.isBlank()) return null;

        String cleaned = rawBhk.trim();

        if (cleaned.toLowerCase().contains("studio")) return 1;

        Matcher matcher = BHK_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.debug("Failed to parse BHK: {}", rawBhk);
            }
        }

        // Try plain digit
        try {
            String digits = cleaned.replaceAll("[^0-9]", "");
            if (!digits.isBlank() && digits.length() == 1) {
                return Integer.parseInt(digits);
            }
        } catch (NumberFormatException e) {
            log.debug("Failed to parse plain BHK: {}", rawBhk);
        }

        return null;
    }
}
