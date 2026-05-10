package com.example.contentcrm.business.workflow;

import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import org.springframework.stereotype.Component;

@Component
public class ContentUnitStatusWorkflow {
    public boolean canDirectTransition(ContentUnitStatus from, ContentUnitStatus to) {
        return (from == ContentUnitStatus.DRAFT && to == ContentUnitStatus.IN_PROGRESS)
                || (from == ContentUnitStatus.NEEDS_CHANGES && to == ContentUnitStatus.IN_PROGRESS);
    }
}
