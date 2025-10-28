package com.mobile.server.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@SpringBootTest()
@ActiveProfiles("local")
public class S3Test {

    @Autowired
    private S3Client s3Client;

    @Test
    void uploadAndDownloadFile() {
        String bucket = "test-bucket";
        String key = "sample3.txt";
        String content = "Hello from LocalStack!22";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromString(content)
        );

        String result = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        ).asUtf8String();

        assertThat(result).isEqualTo(content);
        System.out.println("LocalStack S3 업로드 및 다운로드 성공!");
    }
}
