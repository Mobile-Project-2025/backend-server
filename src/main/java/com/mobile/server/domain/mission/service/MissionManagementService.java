package com.mobile.server.domain.mission.service;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.domain.File;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.dto.RegularMissionCreationDto;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import com.mobile.server.domain.regularMission.domain.RegularMission;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import com.mobile.server.util.file.FileResourceMap;
import com.mobile.server.util.file.S3Uploader;
import lombok.RequiredArgsConstructor;
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


    @Transactional
    public void createRegularMission(RegularMissionCreationDto mission, Long userId) {
        isAdmin(userId);
        String iconImageUrl = getCategoryIconImageUrl(mission.getCategory());
        String bannerImageUrl = getRegularBannerImageUrl();
        RegularMission newMission = createNewMission(mission, iconImageUrl, bannerImageUrl);
        regularMissionRepository.save(newMission);
        saveFile(mission.getMissionImage());
    }

    private void saveFile(MultipartFile file) {
        if (file != null) {
            File newFile = File.ofFile(s3Uploader.makeMetaData(file));
            fileRepository.save(newFile);
            s3Uploader.uploadFile(newFile.getFileKey(), file);
        }
    }


    private RegularMission createNewMission(RegularMissionCreationDto mission, String iconImageUrl,
                                            String bannerImageUrl) {
        return RegularMission.builder().title(mission.getTitle()).missionPoint(mission.getPoint())
                .content(mission.getContent()).iconUrl(iconImageUrl).bannerUrl(bannerImageUrl).build();
    }

    private String getRegularBannerImageUrl() {
        return FileResourceMap.BANNER_MAP.get(FileResourceMap.BANNER_MAP.get(
                MissionType.SCHEDULED.name()));
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
