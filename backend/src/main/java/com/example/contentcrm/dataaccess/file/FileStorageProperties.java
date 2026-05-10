package com.example.contentcrm.dataaccess.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.files")
public record FileStorageProperties(String uploadDir, long maxSizeMb) {
}
