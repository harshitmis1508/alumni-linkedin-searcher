package com.alumni.searcher.controller;

import com.alumni.searcher.dto.AlumniResponse;
import com.alumni.searcher.dto.AlumniSearchRequest;
import com.alumni.searcher.dto.ApiResponse;
import com.alumni.searcher.service.AlumniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/alumni", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Alumni", description = "Search and retrieve alumni LinkedIn profiles")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @Operation(summary = "Search alumni via PhantomBuster and save them to the database")
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<AlumniResponse>>> search(@Valid @RequestBody AlumniSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(alumniService.searchAndSave(request)));
    }

    @Operation(summary = "Get all alumni profiles saved in the database")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AlumniResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(alumniService.getAll()));
    }
}
