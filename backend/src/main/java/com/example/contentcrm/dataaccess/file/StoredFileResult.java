package com.example.contentcrm.dataaccess.file;

public record StoredFileResult(
        String originalName,
        String storedName,
        String mimeType,
        long size,
        String path
) {
}
