package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.dto.postLike.PostLikerDto;
import com.zero.cohousesever.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
     * 특정 게시글의 좋아요 삭제
     */
    void deleteByPostId(Long postId);

    /**
     * 특정 게시글의 좋아요 개수
     */
    long countByPostId(Long postId);

    /**
     * PostLike ↔ Member 조인 후 바로 PostLikerDto로 매핑 (최신 생성순)
     */
    @Query("""
            select new com.zero.cohousesever.post.dto.postLike.PostLikerDto(
                     m.id,
                     m.name,
                     m.profileImageUrl
                   )
            from PostLike pl
              join com.zero.cohousesever.member.entity.Member m
                on m.id = pl.memberId
            where pl.postId = :postId
            order by pl.createdAt desc
            """)
    List<PostLikerDto> findLikerDtosByPostIdOrderByCreatedDesc(@Param("postId") Long postId);

}