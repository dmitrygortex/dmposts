package com.example.contentcrm.presentation.dto.publication;

import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import com.example.contentcrm.presentation.dto.media.MediaFileResponse;

import java.util.List;

public record ManualPublicationResponse(
        ContentUnitResponse contentUnit,
        PublicationVariantResponse variant,
        String platformUrl,
        List<MediaFileResponse> mediaFiles
) {
}
