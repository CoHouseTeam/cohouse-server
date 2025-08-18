package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.*;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시글 목록 조회 - 검색(키워드) 없이, 탭 전환용 타입 필터 + 페이지네이션만 제공합니다.
     */
    public PostListResponse<PostSummaryResponse> getPostList(Long groupId,
                                                             Integer page,
                                                             Integer size,
                                                             PostType type) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 10 : Math.min(size, 100);

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(p, s, sort);

        Page<Post> result = (type != null)
                ? postRepository.findByGroupIdAndType(groupId, type, pageable)
                : postRepository.findByGroupId(groupId, pageable);

        List<PostSummaryResponse> content = result.getContent()
                .stream()
                .map(PostSummaryResponse::from)
                .toList();

        return PostListResponse.of(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
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

        return PostResponse.from(saved);
    }


    /**
     * 게시글 상세 조회
     */
    public PostResponse getPostDetail(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        return PostResponse.from(post);
    }

    /**
     * 게시글 수정
     */
    public PostResponse update(Long id, PostUpdateRequest request) {
        Post post = postRepository.findByIdAndStatus(id, PostStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("post not found or deleted"));

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getType() != null) {
            post.setType(request.getType());
        }

        Post saved = postRepository.save(post);

        return PostResponse.from(saved);
    }

    /**
     * 게시글 삭제
     * - ACTIVE인 글만 삭제 가능
     * - 대상이 없거나 이미 삭제된 경우 NoSuchElementException
     */
    public void deletePost(Long id) {
        Post post = postRepository.findByIdAndStatus(id, PostStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("post not found or already deleted"));
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

//    /**
//     * 게시글 상단 고정
//     */
//    public void pinPost(Long postId) {
//
//    }

}