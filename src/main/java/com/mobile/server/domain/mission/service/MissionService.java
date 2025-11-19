package com.mobile.server.domain.mission.service;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.dto.MissionDetailDto;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import com.mobile.server.domain.missionParticipation.repository.MissionParticipationRepository;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MissionService {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final MissionParticipationRepository missionParticipationRepository;

    public MissionDetailDto getMissionDetail(Long userId, Long missionId) {
        User user = findUserById(userId);
        validateStudent(user);

        Mission mission = findMissionById(missionId);

        Optional<MissionParticipation> participation =
                missionParticipationRepository.findByMissionAndUser(mission, user);
        boolean hasSubmitted = participation.isPresent();

        return buildMissionDetailDto(mission, hasSubmitted);
    }

    private MissionDetailDto buildMissionDetailDto(Mission mission, boolean hasSubmitted) {
        MissionDetailDto.MissionDetailDtoBuilder builder = MissionDetailDto.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .content(mission.getContent())
                .missionPoint(mission.getMissionPoint())
                .category(mission.getCategory())
                .iconImageUrl(mission.getIconUrl())
                .bannerImageUrl(mission.getBannerUrl())
                .startDate(mission.getStartDate())
                .deadLine(mission.getDeadLine())
                .missionType(mission.getMissionType())
                .status(mission.getStatus())
                .hasSubmitted(hasSubmitted);

        // 돌발 미션인 경우 참여자 수 포함
        if (mission.getMissionType() == MissionType.EVENT) {
            builder.participationCount(mission.getParticipationCount());
        }

        return builder.build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
    }

    private Mission findMissionById(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.MISSION_NOT_FOUND));
    }

    private void validateStudent(User user) {
        if (!user.getRole().equals(RoleType.STUDENT)) {
            throw new BusinessException(BusinessErrorCode.URL_FORBIDDEN);
        }
    }
}

