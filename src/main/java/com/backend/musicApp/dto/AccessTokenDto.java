package com.backend.musicApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenDto(@JsonProperty("access_token") String accessToken,
                             @JsonProperty("refresh_token") String refreshToken) {
}
