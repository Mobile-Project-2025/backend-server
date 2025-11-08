package com.mobile.server.domain.mission.dto.dto;


import com.mobile.server.domain.auth.entity.User;
import java.time.LocalDateTime;

public record PendingMissionQueryDto(Long id, LocalDateTime createdAt, User use, String fileKey) {
}
