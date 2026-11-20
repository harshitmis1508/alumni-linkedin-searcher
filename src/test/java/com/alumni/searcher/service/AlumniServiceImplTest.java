package com.alumni.searcher.service;

import com.alumni.searcher.dto.AlumniResponse;
import com.alumni.searcher.dto.AlumniSearchRequest;
import com.alumni.searcher.entity.Alumni;
import com.alumni.searcher.exception.PhantomBusterException;
import com.alumni.searcher.mapper.AlumniMapper;
import com.alumni.searcher.provider.AlumniCandidate;
import com.alumni.searcher.provider.AlumniDataProvider;
import com.alumni.searcher.provider.AlumniSearchCriteria;
import com.alumni.searcher.repository.AlumniRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumniServiceImplTest {

    @Mock
    private AlumniDataProvider provider;
    @Mock
    private AlumniRepository repository;
    @Captor
    private ArgumentCaptor<List<Alumni>> savedCaptor;

    private AlumniServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AlumniServiceImpl(provider, repository, new AlumniMapper());
    }

    private static AlumniCandidate candidate(String name, String url, Integer year) {
        return new AlumniCandidate(name, "Software Engineer at XYZ", "Software Engineer",
                "New York, NY", url, year);
    }

    @Test
    void searchAndSave_persistsNewProfilesAndReturnsThem() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class)))
                .thenReturn(List.of(candidate("John Doe", "https://linkedin.com/in/john", 2020)));
        when(repository.findByLinkedinUrl("https://linkedin.com/in/john")).thenReturn(Optional.empty());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<AlumniResponse> result = service.searchAndSave(
                new AlumniSearchRequest("  University of XYZ ", " Software Engineer ", 2020));

        assertThat(result).hasSize(1);
        AlumniResponse r = result.get(0);
        assertThat(r.name()).isEqualTo("John Doe");
        assertThat(r.currentRole()).isEqualTo("Software Engineer");
        assertThat(r.university()).isEqualTo("University of XYZ");
        assertThat(r.location()).isEqualTo("New York, NY");
        assertThat(r.linkedinHeadline()).isEqualTo("Software Engineer at XYZ");
        assertThat(r.passoutYear()).isEqualTo(2020);
    }

    @Test
    void searchAndSave_passesTrimmedCriteriaToProvider() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class))).thenReturn(List.of());

        service.searchAndSave(new AlumniSearchRequest("  MIT ", " Data Scientist ", null));

        ArgumentCaptor<AlumniSearchCriteria> captor = ArgumentCaptor.forClass(AlumniSearchCriteria.class);
        verify(provider).fetchAlumni(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new AlumniSearchCriteria("MIT", "Data Scientist", null));
    }

    @Test
    void searchAndSave_updatesExistingProfileInsteadOfDuplicating() {
        Alumni existing = new Alumni();
        existing.setName("Old Name");
        existing.setLinkedinUrl("https://linkedin.com/in/john");
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class)))
                .thenReturn(List.of(candidate("John Doe", "https://linkedin.com/in/john", 2020)));
        when(repository.findByLinkedinUrl("https://linkedin.com/in/john")).thenReturn(Optional.of(existing));
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.searchAndSave(new AlumniSearchRequest("XYZ", "Software Engineer", null));

        verify(repository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(1);
        assertThat(savedCaptor.getValue().get(0)).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("John Doe");
    }

    @Test
    void searchAndSave_skipsDuplicateUrlsWithinSameBatch() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class))).thenReturn(List.of(
                candidate("John Doe", "https://linkedin.com/in/john", 2020),
                candidate("John D.", "https://linkedin.com/in/john", 2020)));
        when(repository.findByLinkedinUrl(anyString())).thenReturn(Optional.empty());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.searchAndSave(new AlumniSearchRequest("XYZ", "Software Engineer", null));

        verify(repository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(1);
    }

    @Test
    void searchAndSave_fallsBackToRequestedPassoutYearWhenProviderHasNone() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class)))
                .thenReturn(List.of(candidate("Jane Smith", "https://linkedin.com/in/jane", null)));
        when(repository.findByLinkedinUrl(anyString())).thenReturn(Optional.empty());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<AlumniResponse> result = service.searchAndSave(
                new AlumniSearchRequest("XYZ", "Data Scientist", 2019));

        assertThat(result.get(0).passoutYear()).isEqualTo(2019);
    }

    @Test
    void searchAndSave_savesProfilesWithoutUrlWithoutLookup() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class)))
                .thenReturn(List.of(candidate("No Url", null, 2018)));
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<AlumniResponse> result = service.searchAndSave(new AlumniSearchRequest("XYZ", "Engineer", null));

        assertThat(result).hasSize(1);
        verify(repository, never()).findByLinkedinUrl(any());
    }

    @Test
    void searchAndSave_returnsEmptyListWhenProviderFindsNothing() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class))).thenReturn(List.of());

        assertThat(service.searchAndSave(new AlumniSearchRequest("XYZ", "Engineer", null))).isEmpty();
    }

    @Test
    void searchAndSave_propagatesProviderFailureAndSavesNothing() {
        when(provider.fetchAlumni(any(AlumniSearchCriteria.class)))
                .thenThrow(new PhantomBusterException("boom"));

        assertThatThrownBy(() -> service.searchAndSave(new AlumniSearchRequest("XYZ", "Engineer", null)))
                .isInstanceOf(PhantomBusterException.class)
                .hasMessage("boom");
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void getAll_mapsAllSavedProfiles() {
        Alumni a = new Alumni();
        a.setName("John Doe");
        a.setUniversity("XYZ");
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(a));

        List<AlumniResponse> result = service.getAll();

        assertThat(result).extracting(AlumniResponse::name).containsExactly("John Doe");
    }
}
