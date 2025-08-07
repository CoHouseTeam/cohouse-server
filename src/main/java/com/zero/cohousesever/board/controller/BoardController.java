package com.zero.cohousesever.board.controller;

import com.zero.cohousesever.board.dto.PostRequest;
import com.zero.cohousesever.board.dto.PostResponse;
import com.zero.cohousesever.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시글 API

    /**
     * 그룹 게시판 조회
     */
    @GetMapping("/posts/group/{groupId}")
    public ResponseEntity<List<PostResponse>> getPostListByGroup(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 작성
     */
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest request) {
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequest request
    ) {
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        return ResponseEntity.noContent().build();
    }

//    /**
//     * 공지 게시글 상단 고정
//     */
//    @PatchMapping("/posts/{postId}/pin")
//    public ResponseEntity<Void> pinPost(@PathVariable Long postId) {
//        return ResponseEntity.ok().build();
//    }

    // 좋아요 API

    /**
     * 좋아요(읽음) 누르기
     */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }

    /**
     * 좋아요 취소
     */
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId) {
        return ResponseEntity.noContent().build();
    }

    /**
     * 해당 게시글을 좋아요한 유저 목록 조회
     */
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<List<Long>> getLikeUsers(@PathVariable Long postId) {
        return ResponseEntity.ok().build();
    }

}
