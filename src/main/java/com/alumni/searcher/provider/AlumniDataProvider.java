package com.alumni.searcher.provider;

import java.util.List;

/**
 * Strategy interface: any source of alumni data (PhantomBuster, mock, ...).
 */
public interface AlumniDataProvider {

    List<AlumniCandidate> fetchAlumni(AlumniSearchCriteria criteria);
}
