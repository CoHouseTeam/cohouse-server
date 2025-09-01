package com.zero.cohousesever.post.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글-회원 간 '좋아요' 관계를 나타내는 엔티티입니다.
 * 중복 좋아요를 막기 위해 (post_id, member_id)에 유니크 제약을 둡니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "post_likes",
        uniqueConstraints = {
                // 동일 게시글에 동일 회원이 중복으로 좋아요를 누르지 못하도록 DB 차원에서 보장
                @UniqueConstraint(name = "uk_post_like_post_member", columnNames = {"post_id", "member_id"})
        },
        indexes = {
                // 게시글 단위로 좋아요 수를 셀 때 조회 최적화를 위한 인덱스
                @Index(name = "idx_post_like_post_id", columnList = "post_id")
        }
)
public class PostLike extends BaseEntity {

    @Column(name = "post_id",nullable = false)
    private Long postId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

}
