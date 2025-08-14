package com.zero.cohousesever.post.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Column(nullable = false)
    private Long groupMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Long likeCount = 0L;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PostStatus status = PostStatus.ACTIVE;

}
