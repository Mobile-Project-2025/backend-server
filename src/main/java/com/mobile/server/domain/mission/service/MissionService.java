package com.mobile.server.domain.mission.service;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.domain.File;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.dto.MissionDetailDto;
import com.mobile.server.domain.mission.dto.MissionSubmitResponseDto;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import com.mobile.server.domain.missionParticipation.repository.MissionParticipationRepository;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import com.mobile.server.util.file.S3Uploader;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MissionService {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final MissionParticipationRepository missionParticipationRepository;
    private final FileRepository fileRepository;
    private final S3Uploader s3Uploader;

    public MissionDetailDto getMissionDetail(Long userId, Long missionId) {
        User user = findUserById(userId);
        validateStudent(user);

        Mission mission = findMissionById(missionId);

        Optional<MissionParticipation> participation =
                missionParticipationRepository.findByMissionAndUser(mission, user);
        boolean hasSubmitted = participation.isPresent();

        return buildMissionDetailDto(mission, hasSubmitted);
    }

    @Transactional
    public MissionSubmitResponseDto submitMission(Long userId, Long missionId, MultipartFile photo) {
        User user = findUserById(userId);
        validateStudent(user);

        Mission mission = findMissionById(missionId);

        validateMissionStatus(mission);
        validateDeadline(mission);
        validateDuplicateSubmission(mission, user);

        MissionParticipation participation = MissionParticipation.builder()
                .mission(mission)
                .user(user)
                .participationStatus(MissionParticipationStatus.PENDING)
                .build();
        MissionParticipation savedParticipation = missionParticipationRepository.save(participation);

        saveParticipationFile(savedParticipation, photo);
        mission.incrementParticipationCount();

        return MissionSubmitResponseDto.builder()
                .participationId(savedParticipation.getId())
                .message("미션이 성공적으로 제출되었습니다.")
                .submittedAt(savedParticipation.getCreatedAt())
                .build();
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

    private void validateMissionStatus(Mission mission) {
        if (mission.getStatus() == MissionStatus.CLOSED) {
            throw new BusinessException(BusinessErrorCode.ALREADY_CLOSED_MISSION);
        }
    }

    private void validateDeadline(Mission mission) {
        if (LocalDate.now().isAfter(mission.getDeadLine())) {
            throw new BusinessException(BusinessErrorCode.ALREADY_CLOSED_MISSION);
        }
    }

    private void validateDuplicateSubmission(Mission mission, User user) {
        Optional<MissionParticipation> existing =
                missionParticipationRepository.findByMissionAndUser(mission, user);
        if (existing.isPresent()) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_MISSION_SUBMISSION);
        }
    }

    private void saveParticipationFile(MissionParticipation participation, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            File newFile = File.ofParticipation(participation, s3Uploader.makeMetaData(file));
            fileRepository.save(newFile);
            s3Uploader.uploadFile(newFile.getFileKey(), file);
        }
    }
}
