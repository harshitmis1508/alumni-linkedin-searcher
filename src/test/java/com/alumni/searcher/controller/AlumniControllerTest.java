package com.alumni.searcher.controller;

import com.alumni.searcher.dto.AlumniResponse;
import com.alumni.searcher.dto.AlumniSearchRequest;
import com.alumni.searcher.exception.PhantomBusterException;
import com.alumni.searcher.exception.PhantomBusterTimeoutException;
import com.alumni.searcher.service.AlumniService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlumniController.class)
class AlumniControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlumniService alumniService;

    private static final AlumniResponse JOHN = new AlumniResponse(
            "John Doe", "Software Engineer", "University of XYZ", "New York, NY",
            "Passionate Software Engineer at XYZ Corp", 2020, "https://linkedin.com/in/john");

    @Test
    void search_returns200WithProfiles() throws Exception {
        when(alumniService.searchAndSave(any(AlumniSearchRequest.class))).thenReturn(List.of(JOHN));

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"university":"University of XYZ","designation":"Software Engineer","passoutYear":2020}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(jsonPath("$.data[0].currentRole").value("Software Engineer"))
                .andExpect(jsonPath("$.data[0].university").value("University of XYZ"))
                .andExpect(jsonPath("$.data[0].location").value("New York, NY"))
                .andExpect(jsonPath("$.data[0].linkedinHeadline").value("Passionate Software Engineer at XYZ Corp"))
                .andExpect(jsonPath("$.data[0].passoutYear").value(2020));
    }

    @Test
    void search_acceptsPassoutYearAsString() throws Exception {
        when(alumniService.searchAndSave(any(AlumniSearchRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\",\"passoutYear\":\"2020\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void search_withoutPassoutYear_isAllowed() throws Exception {
        when(alumniService.searchAndSave(any(AlumniSearchRequest.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void search_returns400WhenFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\" \",\"designation\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.length()").value(2));

        verify(alumniService, never()).searchAndSave(any());
    }

    @Test
    void search_returns400ForInvalidPassoutYear() throws Exception {
        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\",\"passoutYear\":1800}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void search_returns400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void search_returns415ForWrongContentType() throws Exception {
        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void search_returns502WhenPhantomBusterFails() throws Exception {
        when(alumniService.searchAndSave(any(AlumniSearchRequest.class)))
                .thenThrow(new PhantomBusterException("PhantomBuster API returned HTTP 401"));

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("PhantomBuster API returned HTTP 401"));
    }

    @Test
    void search_returns504WhenPhantomBusterTimesOut() throws Exception {
        when(alumniService.searchAndSave(any(AlumniSearchRequest.class)))
                .thenThrow(new PhantomBusterTimeoutException("timed out"));

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\"}"))
                .andExpect(status().isGatewayTimeout());
    }

    @Test
    void search_returns500ForUnexpectedErrors() throws Exception {
        when(alumniService.searchAndSave(any(AlumniSearchRequest.class)))
                .thenThrow(new IllegalStateException("db down"));

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void getAll_returnsSavedProfiles() throws Exception {
        when(alumniService.getAll()).thenReturn(List.of(JOHN));

        mockMvc.perform(get("/api/alumni/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
    }
}
