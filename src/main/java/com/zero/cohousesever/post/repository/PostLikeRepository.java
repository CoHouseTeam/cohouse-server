package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {


    /**
     * 특정 게시글-회원 조합의 좋아요 존재 여부 체크 (중복 방지 및 상태 판별)
     */
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 특정 게시글-회원 조합의 좋아요 레코드 삭제 (해제)
     */
    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 특정 게시글의 좋아요 개수
     */
    long countByPostId(Long postId);

}