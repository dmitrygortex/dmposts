package com.example.contentcrm.business.workflow;

import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.model.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusWorkflow {
    public boolean canTransition(TaskStatus from, TaskStatus to, Role actorRole, boolean ownTask) {
        if (actorRole == Role.EXECUTOR) {
            return ownTask && ((from == TaskStatus.TODO && to == TaskStatus.IN_PROGRESS)
                    || (from == TaskStatus.IN_PROGRESS && to == TaskStatus.ON_REVIEW));
        }
        return (from == TaskStatus.TODO && (to == TaskStatus.IN_PROGRESS || to == TaskStatus.CANCELED))
                || (from == TaskStatus.IN_PROGRESS && (to == TaskStatus.ON_REVIEW || to == TaskStatus.CANCELED))
                || (from == TaskStatus.ON_REVIEW && (to == TaskStatus.DONE || to == TaskStatus.IN_PROGRESS));
    }
}
