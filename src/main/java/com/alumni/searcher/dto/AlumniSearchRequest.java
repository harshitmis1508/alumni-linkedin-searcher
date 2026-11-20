package com.alumni.searcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AlumniSearchRequest(
        @Schema(example = "IIT Bombay")
        @NotBlank(message = "university is required")
        String university,

        @Schema(example = "Software Engineer")
        @NotBlank(message = "designation is required")
        String designation,

        @Schema(example = "2020", description = "Optional pass-out year")
        @Min(value = 1950, message = "passoutYear must be >= 1950")
        @Max(value = 2100, message = "passoutYear must be <= 2100")
        Integer passoutYear
) {
}
