package com.example.contentcrm.presentation.dto.publication;

import com.example.contentcrm.business.model.enums.Platform;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkPublicationVariantRequest(
        @NotNull Long contentUnitId,
        @NotEmpty List<Platform> platforms
) {
}
