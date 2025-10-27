package com.mobile.server.domain.file.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileDetailDto {

    private String originalFileName;
    private String key;
    private String contentType;
    private long fileSize;


}
