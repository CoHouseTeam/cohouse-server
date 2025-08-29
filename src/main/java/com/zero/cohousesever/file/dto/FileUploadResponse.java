package com.zero.cohousesever.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponse {
    private String imageUrl;
    private Long settlementAmount;
    private boolean ocrSuccess;
}