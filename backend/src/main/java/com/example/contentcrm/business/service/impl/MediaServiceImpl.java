package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.Role;
import com.example.contentcrm.business.service.MediaService;
import com.example.contentcrm.dataaccess.entity.ContentUnitEntity;
import com.example.contentcrm.dataaccess.entity.MediaFileEntity;
import com.example.contentcrm.dataaccess.entity.TaskEntity;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.file.LocalFileStorage;
import com.example.contentcrm.dataaccess.file.StoredFileResult;
import com.example.contentcrm.dataaccess.repository.ContentUnitRepository;
import com.example.contentcrm.dataaccess.repository.MediaFileRepository;
import com.example.contentcrm.dataaccess.repository.TaskRepository;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.presentation.dto.media.MediaFileResponse;
import com.example.contentcrm.presentation.mapper.MediaFileMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {
    private final MediaFileRepository mediaFileRepository;
    private final ContentUnitRepository contentUnitRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final LocalFileStorage localFileStorage;
    private final MediaFileMapper mediaFileMapper;

    public MediaServiceImpl(
            MediaFileRepository mediaFileRepository,
            ContentUnitRepository contentUnitRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            LocalFileStorage localFileStorage,
            MediaFileMapper mediaFileMapper
    ) {
        this.mediaFileRepository = mediaFileRepository;
        this.contentUnitRepository = contentUnitRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.localFileStorage = localFileStorage;
        this.mediaFileMapper = mediaFileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaFileResponse> list(Long contentUnitId, Long taskId) {
        List<MediaFileEntity> files;
        if (taskId != null) {
            files = mediaFileRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
        } else if (contentUnitId != null) {
            files = mediaFileRepository.findByContentUnitIdOrderByUploadedAtDesc(contentUnitId);
        } else {
            files = mediaFileRepository.findAll();
        }
        return files.stream().map(mediaFileMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public MediaFileResponse upload(MultipartFile file, Long contentUnitId, Long taskId, Long currentUserId) {
        UserEntity user = userRepository.findById(currentUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ContentUnitEntity content = contentUnitRepository.findById(contentUnitId)
                .orElseThrow(() -> new ResourceNotFoundException("Content unit not found"));
        TaskEntity task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
            if (!task.getContentUnit().getId().equals(contentUnitId)) {
                throw new BusinessRuleViolationException("Task does not belong to content unit");
            }
            if (user.getRole() == Role.EXECUTOR && !task.getAssignee().getId().equals(user.getId())) {
                throw new BusinessRuleViolationException("Executor can upload files only to own task");
            }
        } else if (user.getRole() == Role.EXECUTOR) {
            throw new BusinessRuleViolationException("Executor can upload only to own task");
        }
        StoredFileResult stored = localFileStorage.store(file);
        MediaFileEntity entity = new MediaFileEntity();
        entity.setContentUnit(content);
        entity.setTask(task);
        entity.setOriginalName(stored.originalName());
        entity.setStoredName(stored.storedName());
        entity.setMimeType(stored.mimeType());
        entity.setSize(stored.size());
        entity.setPath(stored.path());
        entity.setUploadedBy(user);
        return mediaFileMapper.toResponse(mediaFileRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile download(Long id) {
        MediaFileEntity entity = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file not found"));
        try {
            Resource resource = new UrlResource(localFileStorage.path(entity.getPath()).toUri());
            if (!resource.exists() || !Files.exists(localFileStorage.path(entity.getPath()))) {
                throw new BusinessRuleViolationException("File is missing on disk");
            }
            return new DownloadedFile(resource, entity.getOriginalName(), entity.getMimeType());
        } catch (MalformedURLException e) {
            throw new BusinessRuleViolationException("Invalid file path");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MediaFileEntity entity = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media file not found"));
        localFileStorage.delete(entity.getPath());
        mediaFileRepository.delete(entity);
    }
}
