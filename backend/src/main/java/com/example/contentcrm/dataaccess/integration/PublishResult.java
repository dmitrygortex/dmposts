package com.example.contentcrm.dataaccess.integration;

public record PublishResult(
        boolean success,
        String externalPostId,
        String externalPostUrl,
        String errorMessage,
        String responsePayload
) {
    public static PublishResult success(String externalPostId, String externalPostUrl, String responsePayload) {
        return new PublishResult(true, externalPostId, externalPostUrl, null, responsePayload);
    }

    public static PublishResult failure(String errorMessage, String responsePayload) {
        return new PublishResult(false, null, null, errorMessage, responsePayload);
    }
}
