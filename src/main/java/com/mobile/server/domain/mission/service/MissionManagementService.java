package com.mobile.server.domain.mission.service;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.domain.File;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.dto.dto.EventMissionCreationDto;
import com.mobile.server.domain.mission.dto.dto.MissionResponseDto;
import com.mobile.server.domain.mission.dto.dto.RegularMissionCreationDto;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import com.mobile.server.domain.missionParticipation.repository.MissionParticipationRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import com.mobile.server.domain.regularMission.domain.RegularMission;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import com.mobile.server.util.file.FileResourceMap;
import com.mobile.server.util.file.S3Uploader;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MissionManagementService {
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final RegularMissionRepository regularMissionRepository;
    private final S3Uploader s3Uploader;
    private final MissionRepository missionRepository;
    private final MissionParticipationRepository missionParticipationRepository;


    @Transactional
    public void createRegularMission(RegularMissionCreationDto mission, Long userId) {
        isAdmin(userId);
        String iconImageUrl = getCategoryIconImageUrl(mission.getCategory());
        String bannerImageUrl = getRegularBannerImageUrl();
        RegularMission newMission = createNewRegularMission(mission, iconImageUrl, bannerImageUrl);
        regularMissionRepository.save(newMission);
    }

    @Transactional
    public void createEventMission(EventMissionCreationDto mission, Long userId) {
        isAdmin(userId);
        validateInputDate(mission.getStartDate(), mission.getDeadLine());
        String iconImageUrl = getCategoryIconImageUrl(mission.getCategory());
        String bannerImageUrl = getEventBannerImageUrl();
        Mission newMission = createNewEventMission(mission, iconImageUrl, bannerImageUrl);
        missionRepository.save(newMission);
        saveFile(newMission, mission.getMissionImage());
    }

    public List<MissionResponseDto> getDeadlineMission(Long userId) {
        isAdmin(userId);
        List<Mission> deadlineMission = makeDeadLineMission();
        Set<Mission> uniqueDeadLineMission = new HashSet<>(deadlineMission);
        return makeUniqueMissionResponseList(uniqueDeadLineMission);
    }

    public List<MissionResponseDto> getTerminationMission(Long userId) {
        isAdmin(userId);
        List<Mission> terminationMission = makeTerminationMission();
        Set<Mission> uniqueTerminationMission = new HashSet<>(terminationMission);
        return makeUniqueMissionResponseList(uniqueTerminationMission);
    }

    private List<Mission> makeTerminationMission() {
        return missionRepository.findAllByMissionStatusAndMissionParticipationStatusNot(
                MissionStatus.CLOSED,
                MissionParticipationStatus.PENDING
        );
    }

    private List<Mission> makeDeadLineMission() {
        return missionRepository.findAllByMissionStatusAndMissionParticipationStatus(
                MissionStatus.CLOSED,
                MissionParticipationStatus.PENDING);
    }

    @NotNull
    private List<MissionResponseDto> makeUniqueMissionResponseList(Set<Mission> uniqueMission) {
        return uniqueMission.stream().map(m -> {
            MissionResponseDto result = m.makeMissionResponseDto();
            if (m.getMissionType().equals(MissionType.EVENT)) {
                int count = missionParticipationRepository.findAllByMission_Id(m.getId()).size();
                result.setParticipationCount(count);
            }
            return result;
        }).toList();
    }


    private void saveFile(Mission mission, MultipartFile file) {
        if (mission != null && file != null) {
            File newFile = File.ofMission(mission, s3Uploader.makeMetaData(file));
            fileRepository.save(newFile);
            s3Uploader.uploadFile(newFile.getFileKey(), file);
        }
    }

    private Mission createNewEventMission(EventMissionCreationDto mission, String iconImageUrl, String bannerImageUrl) {
        return Mission.builder()
                .title(mission.getTitle()).content(mission.getContent()).missionPoint(mission.getPoint())
                .missionType(MissionType.EVENT).startDate(mission.getStartDate()).deadLine(mission.getDeadLine())
                .iconUrl(iconImageUrl).bannerUrl(bannerImageUrl)
                .status(
                        mission.getStartDate().isEqual(LocalDate.now())
                                ? MissionStatus.OPEN
                                : MissionStatus.CLOSED
                ).category(mission.getCategory())
                .build();
    }

    private void validateInputDate(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessException(BusinessErrorCode.INVALID_PARAMETER);
        }
    }


    private RegularMission createNewRegularMission(RegularMissionCreationDto mission, String iconImageUrl,
                                                   String bannerImageUrl) {
        return RegularMission.builder().title(mission.getTitle()).missionPoint(mission.getPoint())
                .content(mission.getContent()).iconUrl(iconImageUrl).bannerUrl(bannerImageUrl)
                .category(mission.getCategory())
                .build();
    }

    private String getRegularBannerImageUrl() {
        return FileResourceMap.BANNER_MAP.get(MissionType.SCHEDULED.name());
    }

    private String getEventBannerImageUrl() {
        return FileResourceMap.BANNER_MAP.get(MissionType.EVENT.name());
    }

    private String getCategoryIconImageUrl(String category) {
        String imageUrl = FileResourceMap.ICON_MAP.get(category);
        if (imageUrl == null) {
            throw new BusinessException(BusinessErrorCode.INVALID_CATEGORY);
        }
        return imageUrl;
    }

    private void isAdmin(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        if (!user.getRole().equals(RoleType.ADMIN)) {
            throw new BusinessException(BusinessErrorCode.URL_FORBIDDEN);
        }
    }


}
