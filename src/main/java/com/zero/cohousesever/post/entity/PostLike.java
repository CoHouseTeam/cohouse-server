package com.zero.cohousesever.post.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "post_likes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike extends BaseEntity {

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long groupMemberId;

    private boolean isLiked;

}
