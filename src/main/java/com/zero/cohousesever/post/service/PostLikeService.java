package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    /**
     * 좋아요(읽음) 누르기
     */
    public void likePost(Long postId, Long userId) {

    }

    /**
     * 좋아요 취소
     */
    public void unLikePost(Long postId, Long userId) {

    }

    /**
     * 해당 게시글을 좋아요한 유저 목록 조회
     */
    public List<Long> getLikeUsers(Long postId) {
        return List.of();
    }

    /**
     * 해당 게시글의 좋아요 개수만 조회
     */
    public void countLikesByPostId(Long postId) {
    }
}