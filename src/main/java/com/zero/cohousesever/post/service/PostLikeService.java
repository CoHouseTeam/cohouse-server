package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostLikeCountResponse;
import com.zero.cohousesever.post.dto.PostLikeListResponse;
import com.zero.cohousesever.post.dto.PostLikeStatusResponse;
import com.zero.cohousesever.post.dto.PostLikeToggleResponse;
import com.zero.cohousesever.post.entity.PostLike;
import com.zero.cohousesever.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    /**
     * 좋아요 상태 업데이트 (isLiked=true: 추가 / false: 취소)
     */
    @Transactional // 트랜잭션 경계: 저장/삭제를 원자적으로 처리하기 위해 @Transactional을 사용합니다.
    public PostLikeToggleResponse updateLikeStatus(Long postId, Long memberId) {
        boolean exists = postLikeRepository.existsByPostIdAndMemberId(postId, memberId);

        if (exists) {
            postLikeRepository.deleteByPostIdAndMemberId(postId, memberId);
            long count = postLikeRepository.countByPostId(postId);
            return PostLikeToggleResponse.builder()
                    .postId(postId)
                    .isLiked(false)
                    .likeCount(count)
                    .build();
        }

        try {
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .memberId(memberId)
                    .build();
            postLikeRepository.save(like);

        } catch (DataIntegrityViolationException e) {
            // 드문 동시성 상황: 거의 동시에 같은 (postId, memberId)을 저장 시도하면
            // DB의 유니크 제약(uk_post_like_post_member) 위반 예외가 발생할 수 있습니다.
            // 이 경우 '다른 트랜잭션이 먼저 좋아요를 만든 것'으로 보고 최종 상태를 좋아요로 수렴합니다.
        }

        long count = postLikeRepository.countByPostId(postId);
        return PostLikeToggleResponse.builder()
                .postId(postId)
                .isLiked(true)
                .likeCount(count)
                .build();
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
        return null;
    }
}
