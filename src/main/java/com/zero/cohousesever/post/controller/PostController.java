package com.zero.cohousesever.post.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.post.dto.post.*;
import com.zero.cohousesever.post.service.PostService;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 목록 조회
     * - 탭 전환: type
     * - 페이지네이션: page/size
     * - 상태(status) 반영
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<PostListResponse<PostSummaryResponse>> getPostListByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            // 기본값을 ANNOUNCEMENT(공지)로 강제
            @RequestParam(name = "type", defaultValue = "ANNOUNCEMENT") PostType type,
            // 상태는 선택(미지정 시 Service에서 ACTIVE로 처리)
            @RequestParam(required = false) PostStatus status
    ) {
        return ResponseEntity.ok(
                postService.getPostList(groupId, page, size, type, status)
        );
    }

    /**
     * 게시글 작성
     * - 작성자는 로그인 사용자(principal.id)
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestBody PostRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        PostResponse response = postService.createPost(request, principal.getId());
        URI location = URI.create("/api/posts/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    /**
     * 게시글 상세 조회
     * - ACTIVE 상태만 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostDetail(id));
    }

    /**
     * 게시글 수정
     * - 작성자 본인만 가능(미일치 시 403)
     * - ACTIVE 상태에서만 수정 가능
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        PostResponse updated = postService.update(id, request, principal.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * 게시글 삭제소프트 삭제)
     * - 작성자 본인만 가능(미일치 시 403)
     * - ACTIVE 상태에서만 삭제 가능
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        postService.deletePost(id, principal.getId());
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
