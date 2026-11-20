package com.alumni.searcher.service;

import com.alumni.searcher.dto.AlumniResponse;
import com.alumni.searcher.dto.AlumniSearchRequest;

import java.util.List;

public interface AlumniService {

    /** Fetches alumni from the configured provider, persists them and returns them. */
    List<AlumniResponse> searchAndSave(AlumniSearchRequest request);

    List<AlumniResponse> getAll();
}
