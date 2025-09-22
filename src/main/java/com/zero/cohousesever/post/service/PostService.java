package com.zero.cohousesever.post.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.post.dto.post.*;
import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.event.PostAnnouncementCreatedEvent;
import com.zero.cohousesever.post.repository.PostLikeRepository;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostColor;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final GroupMemberRepository groupMemberRepository;

    private final ApplicationEventPublisher eventPublisher;

    // 페이지네이션 기본 상수
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    /**
     * 게시글 목록 조회 - 검색 없이 타입 필터 + 페이지네이션
     * - 상태(status)는 기본 ACTIVE
     */
    public PostListResponse<PostSummaryResponse> getPostList(
            Long groupId,
            Integer page,
            Integer size,
            PostType type,
            PostStatus status
    ) {
        int p = (page == null || page < 0) ? DEFAULT_PAGE : page - 1;
        int s = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(p, s, sort);

        PostStatus st = (status == null) ? PostStatus.ACTIVE : status;

        // type은 Controller에서 항상 세팅되므로, 상태 + 타입 동시 필터
        Page<Post> result = postRepository.findByGroupIdAndTypeAndStatus(groupId, type, st, pageable);
        Page<PostSummaryResponse> pageResult = result.map(PostSummaryResponse::from);

        return PostListResponse.from(
                pageResult.getContent(),
                pageResult.getNumber() + 1,
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    /**
     * 그룹별 공지사항 요약(제목+날짜) 목록 조회
     */
    public List<AnnouncementSummaryResponse> getAnnouncementsByGroup(Long groupId) {
        List<Post> posts = postRepository.findByGroupIdAndTypeAndStatusOrderByCreatedAtDesc(
                groupId, PostType.ANNOUNCEMENT, PostStatus.ACTIVE);

        return posts.stream()
                .map(post -> AnnouncementSummaryResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .date(post.getCreatedAt().toLocalDate())
                        .build()
                )
                .toList();
    }

    /**
     * 게시글 작성
     * - 작성자는 currentMemberId 사용
     * - 공지는 그룹장만 작성가능
     */
    public PostResponse createPost(PostRequest request, Long currentMemberId) {

        if (PostType.ANNOUNCEMENT.equals(request.getType()) &&
                !groupMemberRepository.existsByGroupIdAndMemberIdAndIsLeaderTrue(request.getGroupId(), currentMemberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ANNOUNCEMENT);
        }

        PostColor color = (request.getColor() != null) ? request.getColor() : PostColor.GRAY;

        Post post = Post.builder()
                .groupId(request.getGroupId())
                .memberId(currentMemberId)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .status(PostStatus.ACTIVE)
                .color(color)
                .build();

        Post saved = postRepository.save(post);

        // 공지일 때만 이벤트 발행 (리스너가 즉시/예약/설정 OFF 처리)
        if (PostType.ANNOUNCEMENT.equals(saved.getType())) {
            eventPublisher.publishEvent(
                    PostAnnouncementCreatedEvent.builder()
                            .groupId(saved.getGroupId())
                            .postId(saved.getId())
                            .build()
            );
        }

        return PostResponse.from(saved);
    }

    /**
     * 게시글 상세 조회
     * - ACTIVE 상태만 조회
     */
    public PostResponse getPostDetail(Long id, Long currentMemberId) {
        Post post = postRepository.findByIdAndStatus(id, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean isAuthor = (currentMemberId != null) && post.getMemberId().equals(currentMemberId);

        return PostResponse.from(post, isAuthor);
    }

    /**
     * 게시글 수정
     * - ACTIVE 상태만
     * - 작성자 본인만
     */
    public PostResponse update(Long id, PostUpdateRequest request, Long currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!PostStatus.ACTIVE.equals(post.getStatus())) {
            throw new CustomException(ErrorCode.POST_ALREADY_DELETED);
        }
        if (!post.getMemberId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getType() != null) {
            post.setType(request.getType());
        }
        if (request.getColor() != null) {
            post.setColor(request.getColor());
        }

        Post saved = postRepository.save(post);
        return PostResponse.from(saved);
    }

    /**
     * 게시글 삭제(소프트 삭제)
     * - ACTIVE 상태만
     * - 작성자 본인만
     */
    @Transactional
    public void deletePost(Long id, Long currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!PostStatus.ACTIVE.equals(post.getStatus())) {
            throw new CustomException(ErrorCode.POST_ALREADY_DELETED);
        }
        if (!post.getMemberId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        postLikeRepository.deleteByPostId(id);

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }
}