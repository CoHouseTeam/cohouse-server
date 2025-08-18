package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.type.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 그룹의 전체 게시글 페이징 조회
     */
    Page<Post> findByGroupId(Long groupId, Pageable pageable);

    /**
     * 그룹의 타입(공지/자유 등) 페이징 조회
     */
    Page<Post> findByGroupIdAndType(Long groupId, PostType type, Pageable pageable);
}