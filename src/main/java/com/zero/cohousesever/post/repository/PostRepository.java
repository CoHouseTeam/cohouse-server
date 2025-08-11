package com.zero.cohousesever.post.repository;

import com.zero.cohousesever.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}