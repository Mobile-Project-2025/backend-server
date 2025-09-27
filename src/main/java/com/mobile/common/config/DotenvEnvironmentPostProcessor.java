package com.mobile.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {

            File dotenvFile = new File(System.getProperty("user.dir"), ".env");

            if (!dotenvFile.exists() || !dotenvFile.isFile()) {
                System.err.println("### .env 파일을 찾을 수 없습니다. 경로: " + dotenvFile.getAbsolutePath());
                return;
            }


            Dotenv dotenv = Dotenv.configure()
                    .directory(dotenvFile.getParent())
                    .filename(dotenvFile.getName())
                    .load();

            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> dotenvMap.put(entry.getKey(), entry.getValue()));

            if (!dotenvMap.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource(DOTENV_PROPERTY_SOURCE_NAME, dotenvMap));
                System.out.println("### .env 파일에서 환경 변수 로드 성공: " + dotenvMap.keySet());
            } else {
                System.out.println("### .env 파일을 로드했지만, 포함된 환경 변수가 없습니다.");
            }

        } catch (Exception e) { // 그 외 모든 예외 처리
            System.err.println("### DotenvEnvironmentPostProcessor 실행 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}