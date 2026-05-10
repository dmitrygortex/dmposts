package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.model.enums.TaskType;
import com.example.contentcrm.dataaccess.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {
    List<TaskEntity> findByContentUnitIdOrderByCreatedAtDesc(Long contentUnitId);

    List<TaskEntity> findByAssigneeIdOrderByDeadlineAsc(Long assigneeId);

    Optional<TaskEntity> findByIdAndAssigneeId(Long id, Long assigneeId);

    long countByContentUnitId(Long contentUnitId);

    boolean existsByContentUnitIdAndAssigneeId(Long contentUnitId, Long assigneeId);

    boolean existsByContentUnitIdAndAssigneeIdAndTypeAndStatusNotIn(Long contentUnitId, Long assigneeId, TaskType type, List<TaskStatus> statuses);

    long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    long countByDeadlineBeforeAndStatusNotIn(LocalDateTime deadline, List<TaskStatus> statuses);

    long countByAssigneeIdAndDeadlineBeforeAndStatusNotIn(Long assigneeId, LocalDateTime deadline, List<TaskStatus> statuses);
}
