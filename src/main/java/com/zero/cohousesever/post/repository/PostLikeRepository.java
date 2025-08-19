package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPostId(Long postId);

    Optional<PostLike> findByPostIdAndGroupMemberId(Long postId, Long groupMemberId);

    List<PostLike> findAllByPostIdAndIsLikedTrue(Long postId);
}