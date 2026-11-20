package com.alumni.searcher.service;

import com.alumni.searcher.dto.AlumniResponse;
import com.alumni.searcher.dto.AlumniSearchRequest;
import com.alumni.searcher.entity.Alumni;
import com.alumni.searcher.mapper.AlumniMapper;
import com.alumni.searcher.provider.AlumniCandidate;
import com.alumni.searcher.provider.AlumniDataProvider;
import com.alumni.searcher.provider.AlumniSearchCriteria;
import com.alumni.searcher.repository.AlumniRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AlumniServiceImpl implements AlumniService {

    private static final Logger log = LoggerFactory.getLogger(AlumniServiceImpl.class);

    private final AlumniDataProvider provider;
    private final AlumniRepository repository;
    private final AlumniMapper mapper;

    public AlumniServiceImpl(AlumniDataProvider provider, AlumniRepository repository, AlumniMapper mapper) {
        this.provider = provider;
        this.repository = repository;
        this.mapper = mapper;
    }

    // Not @Transactional on purpose: the external scrape can take minutes and must not hold a DB connection.
    @Override
    public List<AlumniResponse> searchAndSave(AlumniSearchRequest request) {
        AlumniSearchCriteria criteria = new AlumniSearchCriteria(
                request.university().trim(), request.designation().trim(), request.passoutYear());

        List<AlumniCandidate> candidates = provider.fetchAlumni(criteria);
        log.info("Provider returned {} profiles for university='{}', designation='{}'",
                candidates.size(), criteria.university(), criteria.designation());

        List<Alumni> toSave = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();
        for (AlumniCandidate candidate : candidates) {
            String url = candidate.linkedinUrl();
            if (StringUtils.hasText(url) && !seenUrls.add(url)) {
                continue; // duplicate inside same batch
            }
            toSave.add(upsert(candidate, criteria));
        }

        return repository.saveAll(toSave).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<AlumniResponse> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Alumni upsert(AlumniCandidate candidate, AlumniSearchCriteria criteria) {
        if (StringUtils.hasText(candidate.linkedinUrl())) {
            return repository.findByLinkedinUrl(candidate.linkedinUrl())
                    .map(existing -> {
                        mapper.apply(existing, candidate, criteria);
                        return existing;
                    })
                    .orElseGet(() -> mapper.toEntity(candidate, criteria));
        }
        return mapper.toEntity(candidate, criteria);
    }
}
