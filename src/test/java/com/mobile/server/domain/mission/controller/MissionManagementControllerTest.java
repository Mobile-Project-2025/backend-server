package com.mobile.server.domain.mission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.server.domain.auth.repository.UserRepository;
import com.mobile.server.domain.file.respository.FileRepository;
import com.mobile.server.domain.regularMission.RegularMissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MissionManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegularMissionRepository regularMissionRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long adminId;
    private Long userId;
    

    @Test
    void createRegularMission() {

    }
}