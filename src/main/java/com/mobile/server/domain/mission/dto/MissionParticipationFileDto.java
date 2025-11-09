package com.mobile.server.domain.mission.dto;


import com.mobile.server.domain.auth.entity.User;
import java.time.LocalDateTime;

public record MissionParticipationFileDto(
        Long participationId,
        LocalDateTime createdAt,
        User user,
        String fileKey) {
}
