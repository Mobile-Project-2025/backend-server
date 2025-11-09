package com.mobile.server.util.file;


import com.mobile.server.domain.file.dto.FileDetailDto;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {
    private final S3Template s3Template;
    // s3 bucket name
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    //최대 사진 size
    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxSizeByte;


    //업로드 가능한 파일 List
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "heic",
            "webp");


    //파일 메타 데이터 생성
    public FileDetailDto makeMetaData(MultipartFile file) {
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        String key = getUniqueFileName(originalFilename);
        String contentType = file.getContentType();
        return FileDetailDto.builder().originalFileName(originalFilename).key(key).contentType(contentType)
                .fileSize(file.getSize()).build();
    }

    //단일 파일 업로드
    public void uploadFile(String upLoadKey, MultipartFile file) {
        validateFile(file);
        uploadToS3(file, upLoadKey);
    }


    //단일 파일 수정
    public void correctFile(String upLoadKey, MultipartFile file) {
        uploadFile(upLoadKey, file);
    }


    //파일 하나에 대한 url 반환
    public String getUrlFile(String key) {
        validateKey(key);
        try {
            return s3Template.createSignedGetURL(bucket, key, Duration.ofMinutes(10)).toString();
        } catch (S3Exception e) {
            log.error("s3 파일 가져오기에 실패했습니다. key: {}, Error:{}", key, e.getMessage());
            throw new BusinessException(BusinessErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    //여러 파일에 대한 url 반환
    //실패하면 전부 실패
    public List<String> getUrlFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.FILE_NOT_FOUND);
        }
        return keys.stream().map(this::getUrlFile).collect(Collectors.toList());
    }


    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException(BusinessErrorCode.FILE_NOT_FOUND);
        }
    }

    //내부 파일 검증
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.FILE_EMPTY);
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(BusinessErrorCode.FILENAME_MISSING);
        }
        if (file.getSize() > maxSizeByte.toBytes()) {
            throw new BusinessException(BusinessErrorCode.FILE_TOO_LARGE);
        }

        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(BusinessErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    //s3에 파일 업로드
    private void uploadToS3(MultipartFile file, String key) {
        try {
            s3Template.upload(bucket, key, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAIL, e);
        } catch (S3Exception e) {
            log.error("s3 파일 업로드에 실패했습니다. key: {}, Error: {}", key, e.getMessage());
            throw new BusinessException(BusinessErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    //파일에대한 Key 생성
    private String getUniqueFileName(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + originalFilename;
    }


}
