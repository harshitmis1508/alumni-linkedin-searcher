package com.alumni.searcher.mapper;

import com.alumni.searcher.dto.AlumniResponse;
import com.alumni.searcher.entity.Alumni;
import com.alumni.searcher.provider.AlumniCandidate;
import com.alumni.searcher.provider.AlumniSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class AlumniMapper {

    public Alumni toEntity(AlumniCandidate candidate, AlumniSearchCriteria criteria) {
        Alumni alumni = new Alumni();
        apply(alumni, candidate, criteria);
        return alumni;
    }

    /** Overwrites mutable fields of an existing record with fresher scraped data. */
    public void apply(Alumni alumni, AlumniCandidate candidate, AlumniSearchCriteria criteria) {
        alumni.setName(candidate.name());
        alumni.setCurrentRole(candidate.currentRole() != null ? candidate.currentRole() : criteria.designation());
        alumni.setUniversity(criteria.university());
        alumni.setLocation(candidate.location());
        alumni.setLinkedinHeadline(candidate.headline());
        alumni.setPassoutYear(candidate.passoutYear() != null ? candidate.passoutYear() : criteria.passoutYear());
        alumni.setLinkedinUrl(candidate.linkedinUrl());
    }

    public AlumniResponse toResponse(Alumni a) {
        return new AlumniResponse(a.getName(), a.getCurrentRole(), a.getUniversity(), a.getLocation(),
                a.getLinkedinHeadline(), a.getPassoutYear(), a.getLinkedinUrl());
    }
}
