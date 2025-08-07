package com.zero.cohousesever.board.service;

import com.zero.cohousesever.board.dto.PostRequest;
import com.zero.cohousesever.board.dto.PostResponse;

import java.util.List;

public interface BoardService {

    /**
     * 게시글 작성
     */
    PostResponse createPost(PostRequest request);

    /**
     * 게시글 상세 조회
     */
    PostResponse getPostDetail(Long postId);

    /**
     * 그룹 게시판 조회
     */
    List<PostResponse> getPostListByGroup(Long groupId);

    /**
     * 게시글 수정
     */
    PostResponse updatePost(Long postId, PostRequest request);

    /**
     * 게시글 삭제
     */
    void deletePost(Long postId);

//    /**
//     * 공지 게시글 상단 고정
//     */
//    void pinPost(Long postId);

    /**
     * 좋아요(읽음) 누르기
     */
    void likePost(Long postId, Long userId);

    /**
     * 좋아요 취소
     */
    void unlikePost(Long postId, Long userId);

    /**
     * 해당 게시글을 좋아요한 유저 목록 조회
     */
    List<Long> getLikeUsers(Long postId);
}