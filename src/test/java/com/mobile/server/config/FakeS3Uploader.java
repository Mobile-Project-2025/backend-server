package com.mobile.server.config;

import com.mobile.server.domain.file.dto.FileDetailDto;
import com.mobile.server.util.exception.BusinessErrorCode;
import com.mobile.server.util.exception.BusinessException;
import com.mobile.server.util.file.S3Uploader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


@Component
@Primary
@Profile("test")
public class FakeS3Uploader extends S3Uploader {

    private final Map<String, byte[]> fakeStorage = new ConcurrentHashMap<>();
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "heic", "webp");

    public FakeS3Uploader() {
        super(null);
    }

    @Override
    public FileDetailDto makeMetaData(MultipartFile file) {
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        String key = UUID.randomUUID() + "_" + originalFilename;
        String contentType = file.getContentType();
        return FileDetailDto.builder()
                .originalFileName(originalFilename)
                .key(key)
                .contentType(contentType)
                .fileSize(file.getSize())
                .build();
    }

    @Override
    public void uploadFile(String upLoadKey, MultipartFile file) {
        validateFile(file);
        try {
            fakeStorage.put(upLoadKey, file.getBytes());
        } catch (IOException e) {
            throw new BusinessException(BusinessErrorCode.FILE_UPLOAD_FAIL, e);
        }
    }

    @Override
    public void correctFile(String upLoadKey, MultipartFile file) {
        uploadFile(upLoadKey, file);
    }

    @Override
    public String getUrlFile(String key) {
        validateKey(key);
        if (!fakeStorage.containsKey(key)) {
            throw new BusinessException(BusinessErrorCode.FILE_NOT_FOUND);
        }
        return "https://fake-s3.local/" + key;
    }

    @Override
    public List<String> getUrlFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.FILE_NOT_FOUND);
        }
        return keys.stream().map(this::getUrlFile).toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.FILE_EMPTY);
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(BusinessErrorCode.FILENAME_MISSING);
        }

        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(BusinessErrorCode.INVALID_FILE_TYPE);
        }
    }

    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException(BusinessErrorCode.FILE_NOT_FOUND);
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

    public boolean exists(String key) {
        return fakeStorage.containsKey(key);
    }

    public byte[] getStoredFile(String key) {
        return fakeStorage.get(key);
    }

    public void clearStorage() {
        fakeStorage.clear();
    }
}
