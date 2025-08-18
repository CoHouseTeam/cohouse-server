package com.zero.cohousesever.post.controller;

import com.zero.cohousesever.post.dto.*;
import com.zero.cohousesever.post.service.PostService;
import com.zero.cohousesever.post.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 목록 조회
     * - 탭 전환: type 파라미터로 필터
     * - 페이지네이션: page/size
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<PostListResponse<PostSummaryResponse>> getPostListByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) PostType type
    ) {
        return ResponseEntity.ok(
                postService.getPostList(groupId,page, size, type)
        );
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostDetail(id));
    }

    /**
     * 게시글 수정
     *  존재하지 않거나 삭제(deleted=true)된 경우 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateRequest request
    ) {
        PostResponse updated = postService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        return ResponseEntity.noContent().build(); // 204
    }

//    /**
//     * 공지 게시글 상단 고정
//     */
//    @PatchMapping("/{postId}/pin")
//    public ResponseEntity<Void> pinPost(@PathVariable Long postId) {
//        return ResponseEntity.ok().build();
//    }

}
