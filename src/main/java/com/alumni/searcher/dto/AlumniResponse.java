package com.alumni.searcher.dto;

public record AlumniResponse(
        String name,
        String currentRole,
        String university,
        String location,
        String linkedinHeadline,
        Integer passoutYear,
        String linkedinUrl
) {
}
