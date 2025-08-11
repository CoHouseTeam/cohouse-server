package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}