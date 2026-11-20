package com.alumni.searcher.provider;

/** Provider-agnostic representation of a scraped profile. */
public record AlumniCandidate(
        String name,
        String headline,
        String currentRole,
        String location,
        String linkedinUrl,
        Integer passoutYear
) {
}
