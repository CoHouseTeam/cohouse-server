package com.zero.cohousesever.post.service.impl;

import com.zero.cohousesever.post.dto.PostRequest;
import com.zero.cohousesever.post.dto.PostResponse;
import com.zero.cohousesever.post.repository.PostLikeRepository;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    public PostResponse createPost(PostRequest request) {
        return null;
    }

    @Override
    public PostResponse getPostDetail(Long postId) {
        return null;
    }

    @Override
    public List<PostResponse> getPostListByGroup(Long groupId) {
        return List.of();
    }

    @Override
    public PostResponse updatePost(Long postId, PostRequest request) {
        return null;
    }

    @Override
    public void deletePost(Long postId) {

    }

//    @Override
//    public void pinPost(Long postId) {
//
//    }

    @Override
    public void likePost(Long postId, Long userId) {

    }

    @Override
    public void unlikePost(Long postId, Long userId) {

    }

    @Override
    public List<Long> getLikeUsers(Long postId) {
        return List.of();
    }
}