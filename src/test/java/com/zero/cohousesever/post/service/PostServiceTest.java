package com.zero.cohousesever.post.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.post.dto.post.*;
import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostColor;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT) // 불필요 스텁 경고 완화
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 작성 - 성공 시 PostResponse 반환(작성자는 currentMemberId)")
    void returnsPostResponse_whenCreatePostSuccess() {
        // given
        PostRequest req = new PostRequest();
        req.setGroupId(1L);
        req.setType(PostType.ANNOUNCEMENT);
        req.setTitle("테스트 제목");
        req.setContent("테스트 내용");
        req.setColor(null);

        Long currentMemberId = 5L;

        LocalDateTime now = LocalDateTime.now();
        Post saved = buildPost(
                100L,
                req.getGroupId(),
                currentMemberId,              // 작성자는 인증 사용자
                req.getType(),
                req.getTitle(),
                req.getContent(),
                now,
                now // status=ACTIVE, likeCount=0 기본값
        );

        when(postRepository.save(any(Post.class))).thenReturn(saved);

        // when
        PostResponse res = postService.createPost(req, currentMemberId);

        // then
        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getGroupId()).isEqualTo(1L);
        assertThat(res.getMemberId()).isEqualTo(5L);
        assertThat(res.getType()).isEqualTo(PostType.ANNOUNCEMENT);
        assertThat(res.getTitle()).isEqualTo("테스트 제목");
        assertThat(res.getContent()).isEqualTo("테스트 내용");
        assertThat(res.getCreatedAt()).isNotNull();
        assertThat(res.getUpdatedAt()).isNotNull();

        // 저장 시 memberId가 currentMemberId로 세팅되었는지 검증
        ArgumentCaptor<Post> saveCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().getMemberId()).isEqualTo(currentMemberId);
    }

    @Test
    @DisplayName("게시글 상세 조회 - ACTIVE 상태에서 성공 시 PostResponse 반환")
    void returnsPostResponse_whenGetPostDetailSuccess() {
        //given
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now();

        Post post = buildPost(
                100L,
                1L,
                5L,
                PostType.FREE,
                "상세제목",
                "상세내용",
                created,
                updated,
                PostStatus.ACTIVE,
                PostColor.GRAY
        );

        when(postRepository.findByIdAndStatus(eq(100L), any(PostStatus.class))).thenReturn(Optional.of(post));
        when(postRepository.findById(eq(100L))).thenReturn(Optional.of(post));

        //when
        PostResponse res = postService.getPostDetail(100L);

        //then
        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getGroupId()).isEqualTo(1L);
        assertThat(res.getMemberId()).isEqualTo(5L);
        assertThat(res.getType()).isEqualTo(PostType.FREE);
        assertThat(res.getTitle()).isEqualTo("상세제목");
        assertThat(res.getContent()).isEqualTo("상세내용");
        assertThat(res.getCreatedAt()).isNotNull();
        assertThat(res.getUpdatedAt()).isNotNull();
        assertThat(res.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(res.getColor()).isEqualTo(PostColor.GRAY);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않거나 ACTIVE가 아니면 POST_NOT_FOUND 예외")
    void throwsNotFoundWhenDetailMissing() {
        //given
        when(postRepository.findByIdAndStatus(eq(999L), any(PostStatus.class))).thenReturn(Optional.empty());
        when(postRepository.findById(eq(999L))).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> postService.getPostDetail(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("그룹 + 타입 목록 조회 - type 지정 시 (status 기본 ACTIVE) 페이지 메타 검증")
    void getPostListByGroup_withType_filtersByType_andPaginates() {
        // given
        Long groupId = 2L;
        PostType type = PostType.ANNOUNCEMENT;
        LocalDateTime now = LocalDateTime.now();

        Post p1 = buildPost(21L, groupId, 7L, type, "공지 X", "내용 X", now.minusDays(1), now.minusDays(1));
        Page<Post> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))), 1);

        when(postRepository.findByGroupIdAndTypeAndStatus(eq(groupId), eq(type), eq(PostStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        // when (status=null → ACTIVE 기본)
        PostListResponse<PostSummaryResponse> res = postService.getPostList(groupId, 0, 5, type, null);

        // then
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getType()).isEqualTo(PostType.ANNOUNCEMENT);
        assertThat(res.getTotalElements()).isEqualTo(1);

        // DTO가 1-base 페이지를 내린다면 1 기대, 0-base라면 0으로 바꾸세요.
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(5);

        verify(postRepository, times(1)).findByGroupIdAndTypeAndStatus(eq(groupId), eq(type), eq(PostStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지/사이즈 null 또는 잘못된 값 보정 - 기본값 page=0,size=10 및 최대 100 제한")
    void getPostListByGroup_pageSizeDefaultsAndCaps() {
        // given
        Long groupId = 3L;
        PostType type = PostType.FREE;

        Page<Post> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))), 0);
        when(postRepository.findByGroupIdAndTypeAndStatus(eq(groupId), eq(type), eq(PostStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(empty);

        // when: page=null, size=null → 0,10으로 보정
        postService.getPostList(groupId, null, null, type, null);

        // then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findByGroupIdAndTypeAndStatus(eq(groupId), eq(type), eq(PostStatus.ACTIVE), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(10);

        // when: page=-1, size=1000 → 0, 100(상한)으로 보정
        reset(postRepository);
        when(postRepository.findByGroupIdAndTypeAndStatus(eq(groupId), eq(type), eq(PostStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(empty);

        postService.getPostList(groupId, -1, 1000, type, null);

        verify(postRepository).findByGroupIdAndTypeAndStatus(eq(groupId), eq(type), eq(PostStatus.ACTIVE), pageableCaptor.capture());
        Pageable used2 = pageableCaptor.getValue();
        assertThat(used2.getPageNumber()).isEqualTo(0);
        assertThat(used2.getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("게시글 수정 - null 필드는 유지, 지정 필드만 변경 (작성자 본인)")
    void update_success_partial_withOwner() {
        // given
        Long currentUserId = 5L;
        Post origin = buildPost(
                100L,
                1L,
                currentUserId,                // 작성자 본인
                PostType.ANNOUNCEMENT,
                "old-title",
                "old-content",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                PostStatus.ACTIVE,
                PostColor.GRAY
        );

        when(postRepository.findByIdAndStatus(eq(100L), any(PostStatus.class))).thenReturn(Optional.of(origin));
        when(postRepository.findById(eq(100L))).thenReturn(Optional.of(origin));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostUpdateRequest req = new PostUpdateRequest();
        req.setTitle("new-title");          // 바꾸기
        req.setContent(null);               // 유지
        req.setType(PostType.FREE);         // 바꾸기
        req.setColor(null);                 // 유지

        // when
        PostResponse res = postService.update(100L, req, currentUserId);

        // then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("new-title");
        assertThat(saved.getContent()).isEqualTo("old-content");
        assertThat(saved.getType()).isEqualTo(PostType.FREE);
        assertThat(saved.getColor()).isEqualTo(PostColor.GRAY);
        assertThat(res.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("게시글 수정 - 존재하지 않으면 POST_NOT_FOUND 예외")
    void update_notFound() {
        when(postRepository.findByIdAndStatus(eq(100L), any(PostStatus.class))).thenReturn(Optional.empty());
        when(postRepository.findById(eq(100L))).thenReturn(Optional.empty());

        PostUpdateRequest req = new PostUpdateRequest();
        req.setTitle("x");

        assertThatThrownBy(() -> postService.update(100L, req, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND));
    }

    @Test
    @DisplayName("게시글 수정 - 작성자 불일치 시 UNAUTHORIZED_ACCESS 예외")
    void update_forbidden_whenNotOwner() {
        Long ownerId = 5L;
        Long otherId = 6L;

        Post origin = buildPost(
                100L,
                1L,
                ownerId,                      // 게시글의 작성자
                PostType.FREE,
                "title",
                "content",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                PostStatus.ACTIVE,
                PostColor.GRAY
        );
        when(postRepository.findByIdAndStatus(eq(100L), any(PostStatus.class))).thenReturn(Optional.of(origin));
        when(postRepository.findById(eq(100L))).thenReturn(Optional.of(origin));

        assertThatThrownBy(() -> postService.update(100L, new PostUpdateRequest(), otherId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
    }

    @Test
    @DisplayName("삭제 성공 - ACTIVE 글을 DELETED로 전환 (작성자 본인)")
    void delete_success_withOwner() {
        Long currentUserId = 5L;

        Post post = Post.builder()
                .groupId(1L)
                .memberId(currentUserId)      // 작성자 본인
                .type(PostType.FREE)
                .title("t")
                .content("c")
                .status(PostStatus.ACTIVE)
                .color(PostColor.GRAY)
                .build();
        ReflectionTestUtils.setField(post, "id", 100L);
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now().minusHours(1));

        when(postRepository.findByIdAndStatus(eq(100L), any(PostStatus.class))).thenReturn(Optional.of(post));
        when(postRepository.findById(eq(100L))).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        postService.deletePost(100L, currentUserId);

        // then
        verify(postRepository).save(argThat(p -> p.getStatus() == PostStatus.DELETED));
    }

    @Test
    @DisplayName("삭제 실패 - 대상 없음 → POST_NOT_FOUND 예외")
    void delete_notFound() {
        when(postRepository.findByIdAndStatus(eq(999L), any(PostStatus.class))).thenReturn(Optional.empty());
        when(postRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(999L, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND));
    }

    @Test
    @DisplayName("삭제 실패 - 작성자 불일치 → UNAUTHORIZED_ACCESS 예외")
    void delete_forbidden_whenNotOwner() {
        Long ownerId = 5L;
        Long otherId = 6L;

        Post post = Post.builder()
                .groupId(1L)
                .memberId(ownerId)
                .type(PostType.FREE)
                .title("t")
                .content("c")
                .status(PostStatus.ACTIVE)
                .color(PostColor.GRAY)
                .build();
        ReflectionTestUtils.setField(post, "id", 100L);

        when(postRepository.findByIdAndStatus(eq(100L), any(PostStatus.class))).thenReturn(Optional.of(post));
        when(postRepository.findById(eq(100L))).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(100L, otherId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
    }

    // ------------------------------------------------------
    // helpers
    // ------------------------------------------------------
    // 가장 자주 쓰는 형태(기본 status=ACTIVE, likeCount=0)
    private Post buildPost(Long id, Long groupId,
                           Long memberId, PostType type,
                           String title, String content,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt
    ) {
        return buildPost(id, groupId, memberId, type, title, content, createdAt, updatedAt, PostStatus.ACTIVE, PostColor.GRAY);
    }

    // 전체 필드 지정 버전
    private Post buildPost(Long id, Long groupId,
                           Long memberId, PostType type,
                           String title, String content,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           PostStatus status,
                           PostColor color
    ) {
        Post post = Post.builder()
                .groupId(groupId)
                .memberId(memberId)
                .type(type)
                .title(title)
                .content(content)
                .status(status != null ? status : PostStatus.ACTIVE)
                .color(color != null ? color : PostColor.GRAY)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        ReflectionTestUtils.setField(post, "updatedAt", updatedAt);
        return post;
    }
}