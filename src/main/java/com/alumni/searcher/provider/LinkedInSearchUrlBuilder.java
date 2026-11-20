package com.alumni.searcher.provider;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Builds the LinkedIn people-search URL that the PhantomBuster agent will scrape. */
@Component
public class LinkedInSearchUrlBuilder {

    private static final String BASE = "https://www.linkedin.com/search/results/people/?keywords=";

    public String build(AlumniSearchCriteria criteria) {
        StringBuilder keywords = new StringBuilder()
                .append(criteria.designation()).append(' ')
                .append(criteria.university());
        if (criteria.passoutYear() != null) {
            keywords.append(' ').append(criteria.passoutYear());
        }
        return BASE + URLEncoder.encode(keywords.toString(), StandardCharsets.UTF_8);
    }
}
