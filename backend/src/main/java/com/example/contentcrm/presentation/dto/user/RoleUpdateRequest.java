package com.example.contentcrm.presentation.dto.user;

import com.example.contentcrm.business.model.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull Role role) {
}
