package com.alumni.searcher.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Converts PhantomBuster's result JSON into {@link AlumniCandidate}s.
 * Tolerant to field-name differences between phantoms
 * (fullName/name, profileUrl/linkedinProfileUrl, ...).
 */
@Component
public class PhantomBusterResponseParser {

    private static final Pattern ROLE_SPLIT =
            Pattern.compile("\\s+(?:at|@)\\s+|\\s*\\|\\s*|\\s+-\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern YEAR = Pattern.compile("^(19|20)\\d{2}$");

    public List<AlumniCandidate> parse(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return List.of();
        }
        JsonNode items = root;
        if (root.isObject()) {
            for (String field : List.of("results", "data", "profiles")) {
                if (root.path(field).isArray()) {
                    items = root.get(field);
                    break;
                }
            }
        }
        if (!items.isArray()) {
            return List.of();
        }
        List<AlumniCandidate> candidates = new ArrayList<>();
        for (JsonNode item : items) {
            toCandidate(item).ifPresent(candidates::add);
        }
        return candidates;
    }

    private Optional<AlumniCandidate> toCandidate(JsonNode n) {
        String name = firstText(n, "fullName", "name");
        if (name == null) {
            String first = firstText(n, "firstName");
            String last = firstText(n, "lastName");
            name = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        }
        if (name.isBlank()) {
            return Optional.empty();
        }
        String headline = firstText(n, "headline", "title", "job", "occupation");
        String role = firstText(n, "jobTitle", "currentRole");
        if (role == null) {
            role = extractRole(headline);
        }
        String location = firstText(n, "location", "geoLocation", "region");
        String url = firstText(n, "profileUrl", "linkedinProfileUrl", "defaultProfileUrl", "url");
        Integer year = parseYear(firstText(n, "passoutYear", "educationEndYear", "endYear"));
        return Optional.of(new AlumniCandidate(name, headline, role, location, url, year));
    }

    static String extractRole(String headline) {
        if (headline == null || headline.isBlank()) {
            return null;
        }
        String first = ROLE_SPLIT.split(headline.trim(), 2)[0].trim();
        return first.isEmpty() ? null : first;
    }

    private static Integer parseYear(String value) {
        return (value != null && YEAR.matcher(value).matches()) ? Integer.valueOf(value) : null;
    }

    private static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode v = node.get(field);
            if (v != null && !v.isNull() && !v.asText().isBlank()) {
                return v.asText().trim();
            }
        }
        return null;
    }
}
