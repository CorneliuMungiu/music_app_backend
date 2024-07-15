package com.backend.musicApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActiveTokenDto(@JsonProperty("active") Boolean active) {
}
