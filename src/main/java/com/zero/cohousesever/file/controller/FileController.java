package com.zero.cohousesever.file.controller;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.file.dto.FileUploadResponse;
import com.zero.cohousesever.file.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/profile")
    public ResponseEntity<FileUploadResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
            //TODO 구현 필요
        return ResponseEntity.ok().build();
    }

    /**
     * 영수증 이미지 업로드
     */
    @PostMapping("/receipt")
    public ResponseEntity<FileUploadResponse> uploadReceipt(
            @RequestParam("file") MultipartFile file) throws IOException {

        // 파일 검증
        validateImageFile(file);

        String fileUrl = s3Service.uploadFile(file, "receipts");

        FileUploadResponse response = new FileUploadResponse(fileUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam String fileUrl) {
        String fileName = s3Service.extractFileName(fileUrl);
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok().build();
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.FILE_NOT_IMAGE);
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEED);
        }
    }
}
