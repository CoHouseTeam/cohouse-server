package com.zero.cohousesever.post.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.post.dto.*;
import com.zero.cohousesever.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostLikeController {


    private final PostLikeService postLikeService;

    /**
     * 좋아요/취소
     * - 하트 아이콘 클릭 시 호출되는 엔드포인트입니다.
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeToggleResponse> likeOrUnlike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        PostLikeToggleResponse response = postLikeService.updateLikeStatus(postId, memberId);

        return ResponseEntity.ok(response);
    }

    /**
     * 해당 게시글 좋아요 개수만 조회
     */
    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<PostLikeCountResponse> getLikeCount(@PathVariable Long postId) {

        PostLikeCountResponse response = postLikeService.getLikeCount(postId);

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 사용자 기준 좋아요 여부 조회
     */
    @GetMapping("/{postId}/likes/status")
    public ResponseEntity<PostLikeStatusResponse> getMyLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        PostLikeStatusResponse response = postLikeService.getMyLikeStatus(postId, memberId);

        return ResponseEntity.ok(response);
    }

    /**
     * 해당 게시글 좋아요 사용자 목록
     */
    @GetMapping("/{postId}/likes")
    public ResponseEntity<PostLikeListResponse> getLikeUsers(@PathVariable Long postId) {
        return ResponseEntity.ok(postLikeService.getLikers(postId));
    }
}
