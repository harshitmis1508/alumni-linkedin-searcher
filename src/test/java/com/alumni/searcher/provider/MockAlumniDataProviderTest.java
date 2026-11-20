package com.alumni.searcher.provider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockAlumniDataProviderTest {

    private final MockAlumniDataProvider provider = new MockAlumniDataProvider();

    @Test
    void returnsDeterministicProfilesMatchingCriteria() {
        List<AlumniCandidate> result = provider.fetchAlumni(
                new AlumniSearchCriteria("IIT Bombay", "Software Engineer", 2020));

        assertThat(result).hasSize(5);
        assertThat(result).allSatisfy(c -> {
            assertThat(c.currentRole()).isEqualTo("Software Engineer");
            assertThat(c.passoutYear()).isEqualTo(2020);
            assertThat(c.linkedinUrl()).contains("iit-bombay");
        });
        assertThat(result).extracting(AlumniCandidate::linkedinUrl).doesNotHaveDuplicates();
    }

    @Test
    void usesDifferentUrlsForDifferentUniversities() {
        List<AlumniCandidate> a = provider.fetchAlumni(new AlumniSearchCriteria("MIT", "Engineer", null));
        List<AlumniCandidate> b = provider.fetchAlumni(new AlumniSearchCriteria("Stanford", "Engineer", null));

        assertThat(a.get(0).linkedinUrl()).isNotEqualTo(b.get(0).linkedinUrl());
    }
}
