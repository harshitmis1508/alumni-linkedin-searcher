package com.alumni.searcher.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkedInSearchUrlBuilderTest {

    private final LinkedInSearchUrlBuilder builder = new LinkedInSearchUrlBuilder();

    @Test
    void buildsEncodedUrlWithoutYear() {
        String url = builder.build(new AlumniSearchCriteria("IIT Bombay", "Software Engineer", null));

        assertThat(url).isEqualTo(
                "https://www.linkedin.com/search/results/people/?keywords=Software+Engineer+IIT+Bombay");
    }

    @Test
    void includesYearWhenProvided() {
        String url = builder.build(new AlumniSearchCriteria("MIT", "Data Scientist", 2019));

        assertThat(url).endsWith("keywords=Data+Scientist+MIT+2019");
    }
}
