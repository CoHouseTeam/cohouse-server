package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostRequest;
import com.zero.cohousesever.post.dto.PostResponse;
import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시글 목록 조회
     */
    public List<PostResponse> getAllPosts(Long groupId) {
        return List.of();
    }


    /**
     * 게시글 작성
     */
    public PostResponse createPost(PostRequest request) {
        Post post = Post.builder()
                .groupId(request.getGroupId())
                .memberId(request.getMemberId())
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        Post saved = postRepository.save(post);

        return PostResponse.builder()
                .id(saved.getId())
                .groupId(saved.getGroupId())
                .memberId(saved.getMemberId())
                .type(saved.getType())
                .title(saved.getTitle())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    /**
     * 게시글 상세 조회
     */
    public PostResponse getPostDetail(Long postId) {
        return null;
    }

    /**
     * 게시글 수정
     */
    public PostResponse updatePost(Long postId, PostRequest request) {
        return null;
    }

    /**
     * 게시글 삭제
     */
    public void deletePost(Long postId) {

    }

//    public void pinPost(Long postId) {
//
//    }

}