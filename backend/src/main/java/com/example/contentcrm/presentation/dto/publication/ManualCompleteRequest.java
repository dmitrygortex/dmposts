package com.example.contentcrm.presentation.dto.publication;

import jakarta.validation.constraints.NotBlank;

public record ManualCompleteRequest(@NotBlank String externalPostUrl) {
}
