package com.zero.cohousesever.file.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-url}")
    private String  s3BaseUrl;

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String dirName) throws IOException {
        String fileName = createFileName(file.getOriginalFilename(), dirName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .acl("public-read") // 공개 권한 설정
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    /**
     * 파일명 생성 (중복 방지)
     */
    private String createFileName(String originalFileName, String dirName) {
        return dirName + "/" + UUID.randomUUID() + "_" + originalFileName;
    }

    /**
     * URL에서 경로 추출
     * - 기존 이미지 파일 삭제하기 위함
     */
    public String extractFilePath(String imageUrl) {
        String baseUrl = s3BaseUrl;
        return imageUrl.substring(baseUrl.length());
    }

    /**
     * 이미지 파일 검증
     */
    public void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
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

