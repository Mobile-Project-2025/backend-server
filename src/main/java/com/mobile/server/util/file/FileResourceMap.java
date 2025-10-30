package com.mobile.server.util.file;

import com.mobile.server.domain.mission.constant.MissionCategory;
import com.mobile.server.domain.mission.e.MissionType;
import java.util.Map;

public final class FileResourceMap {
    public static final Map<String, String> ICON_MAP = Map.of(
            MissionCategory.PUBLIC_TRANSPORTATION.name(),
            "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/fc7b54f9-6360-4d31-87a1-7151d7099c39.png",
            MissionCategory.ETC.name(),
            "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/2351f119-f70f-461e-b552-abdb621cffe1.png",
            MissionCategory.RECYCLING.name(),
            "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/d02cd5a5-4469-4efb-bf7e-1191a3594383.png",
            MissionCategory.TUMBLER.name(),
            "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/icons/de7b9a05-1d2f-4588-8835-db6fd8593f3c.png"
    );

    public static final Map<String, String> BANNER_MAP = Map.of(
            MissionType.SCHEDULED.name(),
            "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/011e06d1-3d95-4a66-a4b7-9a2ffcf14280.png"
            , MissionType.EVENT.name(),
            "https://mobile-reple.s3.ap-northeast-2.amazonaws.com/banners/537500d1-4fe2-4f06-9cf3-38e46ed87d64.png"
    );


}
