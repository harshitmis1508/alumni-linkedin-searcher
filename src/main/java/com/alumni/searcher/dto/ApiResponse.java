package com.alumni.searcher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Uniform response envelope: {"status":"success","data":[...]} or
 * {"status":"error","message":"...","errors":[...]}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String status, T data, String message, List<String> errors) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message, null);
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>("error", null, message, errors);
    }
}
