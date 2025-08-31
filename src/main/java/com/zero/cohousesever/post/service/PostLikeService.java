package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.postLike.*;
import com.zero.cohousesever.post.entity.PostLike;
import com.zero.cohousesever.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    /**
     * 좋아요 상태 업데이트 (isLiked=true: 추가 / false: 취소)
     */
    @Transactional
    public PostLikeToggleResponse updateLikeStatus(Long postId, Long memberId) {

        if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId)) {
            postLikeRepository.deleteByPostIdAndMemberId(postId, memberId);

            long countAfter = postLikeRepository.countByPostId(postId);
            return PostLikeToggleResponse.builder()
                    .postId(postId)
                    .isLiked(false)
                    .likeCount(countAfter)
                    .build();
        }

        try {
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .memberId(memberId)
                    .build();

            postLikeRepository.saveAndFlush(like);

        } catch (DataIntegrityViolationException e) {
            boolean nowExists = postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
            if (!nowExists) {
                throw e;
            }
        }

        long countAfter = postLikeRepository.countByPostId(postId);
        return PostLikeToggleResponse.builder()
                .postId(postId)
                .isLiked(true)
                .likeCount(countAfter)
                .build();
    }

    /**
     * 특정 게시글 좋아요 개수만 조회
     */
    public PostLikeCountResponse getLikeCount(Long postId) {
        long count = postLikeRepository.countByPostId(postId);

        return PostLikeCountResponse.builder()
                .postId(postId)
                .count(count)
                .build();
    }

    /**
     * 현재 사용자 기준 좋아요 여부 조회
     */
    public PostLikeStatusResponse getMyLikeStatus(Long postId, Long memberId) {
        boolean isLiked = postLikeRepository.existsByPostIdAndMemberId(postId, memberId);

        return PostLikeStatusResponse.builder()
                .postId(postId)
                .isLiked(isLiked)
                .build();
    }

    /**
     * 특정 게시글 좋아요 사용자 목록
     */
    public PostLikeListResponse getLikers(Long postId) {
        List<PostLikerDto> likers = postLikeRepository.findLikerDtosByPostIdOrderByCreatedDesc(postId);
        int total = likers.size();

        return PostLikeListResponse.builder()
                .postId(postId)
                .totalCount(total)
                .likers(likers)
                .build();
    }
}