package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 상태값(게시중/삭제)으로 게시글 조회 (상세)
     */
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);

    /**
     * 그룹의 타입(공지/자유 등) 페이징 조회 (목록)
     * - 목록 화면은 항상 탭(type) 선택이므로 type + status로만 조회
     */
    Page<Post> findByGroupIdAndTypeAndStatus(Long groupId, PostType type, PostStatus status, Pageable pageable);

}