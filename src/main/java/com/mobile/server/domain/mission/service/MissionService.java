package com.mobile.server.domain.mission.service;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.domain.File;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.dto.MissionDetailDto;
import com.mobile.server.domain.mission.dto.MissionSubmitResponseDto;
import com.mobile.server.domain.mission.dto.ParticipationHistoryDetailDto;
import com.mobile.server.domain.mission.dto.ParticipationHistoryDto;
import com.mobile.server.domain.mission.dto.PendingMissionDto;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public List<PendingMissionDto> getPendingMissions(Long userId) {
        User user = findUserById(userId);
        validateStudent(user);

        List<MissionParticipation> pendingParticipations =
                missionParticipationRepository.findByUserAndParticipationStatusOrderByCreatedAtDesc(
                        user, MissionParticipationStatus.PENDING);

        return pendingParticipations.stream()
                .map(this::convertToPendingMissionDto)
                .collect(Collectors.toList());
    }

    private PendingMissionDto convertToPendingMissionDto(MissionParticipation participation) {
        Mission mission = participation.getMission();

        // 제출한 사진 URL 조회
        String photoUrl = fileRepository.findByParticipationAndIsDeletedFalse(participation)
                .map(file -> s3Uploader.getUrlFile(file.getFileKey()))
                .orElse(null);

        return PendingMissionDto.builder()
                .participationId(participation.getId())
                .missionId(mission.getId())
                .title(mission.getTitle())
                .missionPoint(mission.getMissionPoint())
                .category(mission.getCategory())
                .iconImageUrl(mission.getIconUrl())
                .missionType(mission.getMissionType())
                .participationStatus(participation.getParticipationStatus())
                .submittedPhotoUrl(photoUrl)
                .submittedAt(participation.getCreatedAt())
                .build();
    }

    public List<ParticipationHistoryDto> getParticipationHistory(Long userId) {
        User user = findUserById(userId);
        validateStudent(user);

        List<MissionParticipation> participations =
                missionParticipationRepository.findByUserOrderByCreatedAtDesc(user);

        return participations.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    private ParticipationHistoryDto convertToHistoryDto(MissionParticipation participation) {
        Mission mission = participation.getMission();

        return ParticipationHistoryDto.builder()
                .participationId(participation.getId())
                .missionId(mission.getId())
                .title(mission.getTitle())
                .bannerUrl(mission.getBannerUrl())
                .iconUrl(mission.getIconUrl())
                .missionPoint(mission.getMissionPoint())
                .participationCount(mission.getParticipationCount())
                .participationStatus(participation.getParticipationStatus())
                .participatedAt(participation.getCreatedAt())
                .build();
    }

    public ParticipationHistoryDetailDto getParticipationHistoryDetail(Long userId, Long participationId) {
        User user = findUserById(userId);
        validateStudent(user);

        MissionParticipation participation =
                missionParticipationRepository.findByIdAndUser(participationId, user)
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.PARTICIPATION_NOT_FOUND));


        String submittedPhotoUrl = fileRepository
                .findByParticipationAndIsDeletedFalse(participation)
                .map(file -> s3Uploader.getUrlFile(file.getFileKey()))
                .orElse(null);

        return convertToHistoryDetailDto(participation, submittedPhotoUrl);
    }

    private ParticipationHistoryDetailDto convertToHistoryDetailDto(
            MissionParticipation participation,
            String submittedPhotoUrl
    ) {
        Mission mission = participation.getMission();

        return ParticipationHistoryDetailDto.builder()
                .participationId(participation.getId())
                .missionId(mission.getId())
                .title(mission.getTitle())
                .content(mission.getContent())
                .bannerUrl(mission.getBannerUrl())
                .iconUrl(mission.getIconUrl())
                .missionPoint(mission.getMissionPoint())
                .participationCount(mission.getParticipationCount())
                .category(mission.getCategory())
                .missionType(mission.getMissionType())
                .participationStatus(participation.getParticipationStatus())
                .submittedPhotoUrl(submittedPhotoUrl)
                .participatedAt(participation.getCreatedAt())
                .startDate(mission.getStartDate())
                .deadLine(mission.getDeadLine())
                .build();
    }
}
