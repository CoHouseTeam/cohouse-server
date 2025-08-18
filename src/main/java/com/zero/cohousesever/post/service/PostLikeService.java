package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.*;
import com.zero.cohousesever.post.entity.PostLike;
import com.zero.cohousesever.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    /**
     * 좋아요 상태 업데이트 (isLiked=true: 추가 / false: 취소)
     */
    public PostLikeSimpleResponse updateLikeStatus(Long postId, PostLikeRequest request) {
        return null;
    }

    /**
     * 특정 게시글 좋아요 개수만 조회
     */
    public PostLikeCountResponse getLikeCount(Long postId) {
        return null;
    }

    /**
     * 현재 사용자 기준 좋아요 여부 조회
     */
    public PostLikeStatusResponse getMyLikeStatus(Long postId) {
        return null;
    }

    /**
     * 특정 게시글 좋아요 사용자 목록
     */
    public PostLikeListResponse getLikers(Long postId) {
        return null;
    }
}