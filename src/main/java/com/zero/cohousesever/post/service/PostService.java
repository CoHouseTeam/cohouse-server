package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostListResponse;
import com.zero.cohousesever.post.dto.PostRequest;
import com.zero.cohousesever.post.dto.PostResponse;
import com.zero.cohousesever.post.dto.PostSummaryResponse;
import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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