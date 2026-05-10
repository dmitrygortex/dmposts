package com.example.contentcrm.dataaccess.file;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorage {
    private static final Set<String> ALLOWED_PREFIXES = Set.of("image/", "video/", "application/pdf");
    private final FileStorageProperties properties;

    public LocalFileStorage(FileStorageProperties properties) {
        this.properties = properties;
    }

    public StoredFileResult store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleViolationException("File is empty");
        }
        long maxBytes = properties.maxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleViolationException("File is too large");
        }
        String mimeType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        boolean allowed = ALLOWED_PREFIXES.stream().anyMatch(mimeType::startsWith);
        if (!allowed) {
            throw new BusinessRuleViolationException("Unsupported file type");
        }

        try {
            Files.createDirectories(Path.of(properties.uploadDir()));
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String stored = UUID.randomUUID() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = Path.of(properties.uploadDir()).resolve(stored).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFileResult(original, stored, mimeType, file.getSize(), target.toString());
        } catch (IOException e) {
            throw new BusinessRuleViolationException("File storage error: " + e.getMessage());
        }
    }

    public Path path(String storedPath) {
        return Path.of(storedPath);
    }

    public void delete(String storedPath) {
        try {
            Files.deleteIfExists(Path.of(storedPath));
        } catch (IOException e) {
            throw new BusinessRuleViolationException("File delete error: " + e.getMessage());
        }
    }
}
