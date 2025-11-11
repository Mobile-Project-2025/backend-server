package com.mobile.server.domain.common;

import com.mobile.server.domain.auth.entity.RoleType;
import com.mobile.server.domain.auth.entity.User;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.domain.File;
import com.mobile.server.domain.file.dto.FileDetailDto;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.mission.constant.MissionCategory;
import com.mobile.server.domain.mission.domain.Mission;
import com.mobile.server.domain.mission.e.MissionStatus;
import com.mobile.server.domain.mission.e.MissionType;
import com.mobile.server.domain.mission.repository.MissionRepository;
import com.mobile.server.domain.missionParticipation.domain.MissionParticipation;
import com.mobile.server.domain.missionParticipation.eum.MissionParticipationStatus;
import com.mobile.server.domain.missionParticipation.repository.MissionParticipationRepository;
import com.mobile.server.util.file.FileResourceMap;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MissionParticipationRepository missionParticipationRepository;
    private final MissionRepository missionRepository;
    private final FileRepository fileRepository;

    @Value("${app.admin.username}")
    private String adminId;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.user.username}")
    private String userId;

    @Value("${app.user.password}")
    private String userPassword;


    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByStudentId(adminId).isEmpty()) {
            User admin = User.builder()
                    .nickname("admin")
                    .studentId(adminId)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(RoleType.ADMIN)
                    .cumulativePoint(99999L)
                    .build();
            userRepository.save(admin);
        }

        // 사용자 테스트 데이터 (고정)
        User testUser1 = User.builder().nickname("모바일 프로그래밍 테스트 계정")
                .studentId(userId).password(passwordEncoder.encode(userPassword))
                .role(RoleType.STUDENT)
                .cumulativePoint(1000L)
                .build();
        userRepository.save(testUser1);

        // 승인 대기 미션 (진행 중인 미션)
        Mission mission1 = Mission.builder().title("test1").content("테스트1")
                .missionPoint(10L).missionType(MissionType.EVENT)
                .startDate(LocalDate.now()).deadLine(LocalDate.now().plusDays(30))
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.PUBLIC_TRANSPORTATION.name()))
                .category(MissionCategory.PUBLIC_TRANSPORTATION.name())
                .status(MissionStatus.OPEN)
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.EVENT.name()))
                .participationCount(0).build();
        missionRepository.save(mission1);
        File file1 = File.ofMission(mission1, new FileDetailDto("test.jpg", "test.jpg", "jpg", 30L));
        fileRepository.save(file1);

        // 미션 1번에 대한 참여 (승인 대기 중)
        MissionParticipation testUserParticipation1 = MissionParticipation.builder()
                .participationStatus(MissionParticipationStatus.PENDING)
                .mission(mission1).user(testUser1).build();
        missionParticipationRepository.save(testUserParticipation1);
        File file2 = File.ofParticipation(testUserParticipation1,
                new FileDetailDto("submission.jpg", "submission.jpg", "jpg", 30L));
        fileRepository.save(file2);

        // 마감된 미션 (제출 완료)
        Mission mission2 = Mission.builder().title("test2").content("테스트2")
                .missionPoint(5L).missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now()).deadLine(LocalDate.now().minusDays(1)) // 마감 날짜
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.TUMBLER.name()))
                .category(MissionCategory.TUMBLER.name())
                .status(MissionStatus.CLOSED) // 종료된 상태
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.SCHEDULED.name()))
                .participationCount(0).build();
        missionRepository.save(mission2);
        File file3 = File.ofMission(mission2, new FileDetailDto("test1.jpg", "test1.jpg", "jpg", 30L));
        fileRepository.save(file3);

        // 마감된 미션에 참여한 제출 완료 유저
        MissionParticipation testUserParticipation2 = MissionParticipation.builder()
                .participationStatus(MissionParticipationStatus.PENDING) // 제출 상태
                .mission(mission2).user(testUser1).build();
        missionParticipationRepository.save(testUserParticipation2);
        File file4 = File.ofParticipation(testUserParticipation2,
                new FileDetailDto("test2.jpg", "test2.jpg", "jpg", 30L)); // 제출 파일
        fileRepository.save(file4);

        // 완료된 미션 (제출하지 않음)
        Mission mission3 = Mission.builder().title("test3").content("완료된 미션")
                .missionPoint(20L).missionType(MissionType.EVENT)
                .startDate(LocalDate.now().minusDays(5)).deadLine(LocalDate.now().minusDays(3)) // 완료된 미션
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.ETC.name()))
                .category(MissionCategory.ETC.name())
                .status(MissionStatus.CLOSED) // 종료된 상태
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.EVENT.name()))
                .participationCount(0).build();
        missionRepository.save(mission3);
        File file6 = File.ofMission(mission3, new FileDetailDto("test4.jpg", "test4.jpg", "jpg", 30L));
        fileRepository.save(file6);

        // 고정 미션
        Mission mission4 = Mission.builder().title("test4").content("미션4")
                .missionPoint(15L).missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now()).deadLine(LocalDate.now().plusDays(30)) // 진행 중
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.ETC.name()))
                .category(MissionCategory.ETC.name())
                .status(MissionStatus.OPEN) // 진행 중
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.SCHEDULED.name()))
                .participationCount(0).build();
        missionRepository.save(mission4);
        File file7 = File.ofMission(mission4, new FileDetailDto("test6.jpg", "test6.jpg", "jpg", 30L));
        fileRepository.save(file7);

        // 관리자 승인 대기 미션 (완전 종료됨)
        Mission mission5 = Mission.builder().title("test5").content("승인 대기 미션 종료")
                .missionPoint(30L).missionType(MissionType.EVENT)
                .startDate(LocalDate.now().minusDays(10)).deadLine(LocalDate.now().minusDays(5)) // 이미 종료
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.PUBLIC_TRANSPORTATION.name()))
                .category(MissionCategory.PUBLIC_TRANSPORTATION.name())
                .status(MissionStatus.CLOSED) // 종료 상태
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.EVENT.name()))
                .participationCount(0).build();
        missionRepository.save(mission5);
        File file8 = File.ofMission(mission5, new FileDetailDto("test7.jpg", "test7.jpg", "jpg", 30L));
        fileRepository.save(file8);
    }
}
