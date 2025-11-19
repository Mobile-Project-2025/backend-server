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
        Mission mission1 = Mission.builder().title("대중교통 이용하고 지구 지키기")
                .content("출퇴근길 자가용 대신 버스나 지하철을 이용해보세요.\n탄소 배출을 줄이고 맑은 공기를 되찾을 수 있습니다.\n대중교통 이용 인증샷을 올려주세요!")
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
        Mission mission2 = Mission.builder().title("일회용 컵 대신 텀블러 사용하기").content(
                        "카페에서 플라스틱 컵 대신 개인 텀블러를 사용하세요.\n연간 버려지는 플라스틱 쓰레기를 획기적으로 줄일 수 있습니다.\n주문하신 음료가 담긴 텀블러 사진을 찍어주세요.")
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
        Mission mission3 = Mission.builder().title("안 쓰는 플러그 뽑기 챌린지")
                .content("사용하지 않는 가전제품의 대기 전력을 차단해주세요.\n작은 습관이 모여 큰 에너지를 절약할 수 있습니다.\n멀티탭 전원을 끄거나 플러그를 뽑은 사진을 공유해주세요.")
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
        Mission mission4 = Mission.builder().title("장바구니 이용하고 비닐 줄이기")
                .content("마트 갈 때 비닐봉투 대신 장바구니를 챙겨가세요.\n비닐 사용을 줄여 토양 오염을 막을 수 있습니다.\n물건이 담긴 장바구니 인증샷을 업로드해주세요.")
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
        Mission mission5 = Mission.builder().title("자전거로 출퇴근하기")
                .content("가까운 거리는 자동차 대신 자전거로 이동해보세요.\n건강도 챙기고 온실가스 배출도 줄이는 일석이조 효과!\n자전거 이용 인증 사진을 남겨주세요.")
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

// 상시 미션
        Mission mission6 = Mission.builder().title("올바른 투명 페트병 분리배출")
                .content("생수병의 라벨을 제거하고 깨끗이 씻어서 배출해주세요.\n고품질 재활용 원료로 다시 태어날 수 있습니다.\n라벨을 뗀 투명 페트병 사진을 인증해주세요.")
                .missionPoint(40L).missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now()).deadLine(LocalDate.now().plusDays(30))
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.RECYCLING.name()))
                .category(MissionCategory.RECYCLING.name())
                .status(MissionStatus.OPEN)
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.SCHEDULED.name()))
                .participationCount(0).build();
        missionRepository.save(mission6);
        File file9 = File.ofMission(mission6, new FileDetailDto("test8.jpg", "test8.jpg", "jpg", 30L));
        fileRepository.save(file9);

// 상시 미션2
        Mission mission7 = Mission.builder().title("배달 음식 주문 시 일회용품 거절")
                .content("배달 주문 시 '일회용 수저, 포크 안 주셔도 돼요'를 선택하세요.\n불필요한 플라스틱 쓰레기를 줄이는 첫걸음입니다.\n일회용품 없이 식사하는 모습을 인증해주세요.")
                .missionPoint(300L).missionType(MissionType.SCHEDULED)
                .startDate(LocalDate.now()).deadLine(LocalDate.now().plusDays(20))
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.TUMBLER.name()))
                .category(MissionCategory.TUMBLER.name())
                .status(MissionStatus.OPEN)
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.SCHEDULED.name()))
                .participationCount(0).build();
        missionRepository.save(mission7);
        File file10 = File.ofMission(mission7, new FileDetailDto("test10.jpg", "test10.jpg", "jpg", 30L));
        fileRepository.save(file10);

//돌발 미션
        Mission mission8 = Mission.builder().title("잔반 없는 날! 빈 그릇 인증")
                .content("오늘 점심은 먹을 만큼만 담아 남기지 않고 먹어봐요.\n음식물 쓰레기 처리 과정에서 발생하는 온실가스를 줄입시다.\n깨끗하게 비운 식판이나 그릇 사진을 올려주세요.")
                .missionPoint(500L).missionType(MissionType.EVENT)
                .startDate(LocalDate.now()).deadLine(LocalDate.now().plusDays(7))
                .iconUrl(FileResourceMap.ICON_MAP.get(MissionCategory.ETC.name()))
                .category(MissionCategory.ETC.name())
                .status(MissionStatus.OPEN)
                .bannerUrl(FileResourceMap.BANNER_MAP.get(MissionType.EVENT.name()))
                .participationCount(0).build();
        missionRepository.save(mission8);
        File file11 = File.ofMission(mission8, new FileDetailDto("test11.jpg", "test11.jpg", "jpg", 30L));
        fileRepository.save(file11);

    }
}
