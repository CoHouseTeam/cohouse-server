package com.zero.cohousesever.post.controller;

import com.zero.cohousesever.post.dto.PostLikeCountResponse;
import com.zero.cohousesever.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    /**
     * 좋아요(읽음) 누르기
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }

    /**
     * 좋아요 취소
     */
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<Void> unLikePost(@PathVariable Long postId) {
        return ResponseEntity.noContent().build();
    }

    /**
     * 해당 게시글 좋아요 목록/수 조회
     */
    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<Long>> getLikeUsers(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }

    /**
     * 해당 게시글의 좋아요 개수만 조회
     */
    @GetMapping("/{postId}/likes/counts")
    public ResponseEntity<PostLikeCountResponse> getLikeCount(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }

}
