package com.alumni.searcher.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic fake data so the whole flow (API -> DB) can be demoed
 * without a PhantomBuster subscription or LinkedIn cookie.
 * Active when phantombuster.mock-enabled=true (default).
 */
@Component
@ConditionalOnProperty(prefix = "phantombuster", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockAlumniDataProvider implements AlumniDataProvider {

    private static final List<String> NAMES = List.of(
            "Aarav Sharma", "Priya Nair", "Rohan Mehta", "Ananya Iyer", "Vikram Singh");
    private static final List<String> COMPANIES = List.of(
            "Google", "Microsoft", "Flipkart", "Razorpay", "Atlassian");
    private static final List<String> LOCATIONS = List.of(
            "Bengaluru, Karnataka, India", "Pune, Maharashtra, India", "Hyderabad, Telangana, India",
            "Gurugram, Haryana, India", "Mumbai, Maharashtra, India");

    @Override
    public List<AlumniCandidate> fetchAlumni(AlumniSearchCriteria criteria) {
        List<AlumniCandidate> result = new ArrayList<>();
        for (int i = 0; i < NAMES.size(); i++) {
            String name = NAMES.get(i);
            String headline = criteria.designation() + " at " + COMPANIES.get(i);
            Integer year = criteria.passoutYear() != null ? criteria.passoutYear() : 2016 + i;
            String url = "https://www.linkedin.com/in/mock-" + slug(name) + "-" + slug(criteria.university());
            result.add(new AlumniCandidate(name, headline, criteria.designation(),
                    LOCATIONS.get(i), url, year));
        }
        return result;
    }

    private static String slug(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD).toLowerCase(Locale.ROOT);
        return n.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
