package com.mobile.server.domain.regularMission;

import com.mobile.server.domain.regularMission.domain.RegularMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularMissionRepository extends JpaRepository<RegularMission, Long> {
}
