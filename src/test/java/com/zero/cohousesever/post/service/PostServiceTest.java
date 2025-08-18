package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.*;
import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 작성 - 성공 시 PostResponse 반환")
    void returnsPostResponse_whenCreatePostSuccess() {
        // given
        PostRequest req = new PostRequest();
        req.setGroupId(1L);
        req.setMemberId(5L);
        req.setType(PostType.ANNOUNCEMENT);
        req.setTitle("테스트 제목");
        req.setContent("테스트 내용");

        LocalDateTime now = LocalDateTime.now();
        Post saved = buildPost(
                100L,
                req.getGroupId(),
                req.getMemberId(),
                req.getType(),
                req.getTitle(),
                req.getContent(),
                now,
                now // status=ACTIVE, likeCount=0 기본값 사용
        );

        when(postRepository.save(any(Post.class))).thenReturn(saved);

        // when
        PostResponse res = postService.createPost(req);

        // then
        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getGroupId()).isEqualTo(1L);
        assertThat(res.getMemberId()).isEqualTo(5L);
        assertThat(res.getType()).isEqualTo(PostType.ANNOUNCEMENT);
        assertThat(res.getTitle()).isEqualTo("테스트 제목");
        assertThat(res.getContent()).isEqualTo("테스트 내용");
        assertThat(res.getCreatedAt()).isNotNull();
        assertThat(res.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 상세 조회 - 성공 시 PostResponse 반환")
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
                updated
        );

        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

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
    }

    @Test
    @DisplayName("게시글 상세 조회 - 존재하지 않으면 404 NOT_FOUND 예외를 던진다")
    void throwsNotFoundWhenDetailMissing() {
        //given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> postService.getPostDetail(999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(rse.getReason()).contains("게시글을 찾을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("그룹 전체 목록 조회 - type 미지정 시 findByGroupId 호출 및 페이지 메타 검증")
    void getPostListByGroup_withoutType_returnsPagedList() {
        // given
        Long groupId = 1L;
        LocalDateTime now = LocalDateTime.now();

        Post p1 = buildPost(10L, groupId, 5L, PostType.ANNOUNCEMENT, "공지 A", "내용 A", now.minusHours(2), now.minusHours(2));
        Post p2 = buildPost(9L, groupId, 6L, PostType.FREE, "자유 B", "내용 B", now.minusHours(3), now.minusHours(3));

        Page<Post> page = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))), 2);
        when(postRepository.findByGroupId(eq(groupId), any(Pageable.class))).thenReturn(page);

        // when
        PostListResponse<PostSummaryResponse> res = postService.getPostList(groupId, 0, 10, null);

        // then
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getPage()).isEqualTo(0);
        assertThat(res.getSize()).isEqualTo(10);
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getTotalPages()).isEqualTo(1);
        assertThat(res.isLast()).isTrue();

        // 호출 메서드/페이지 요청 검증
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository, times(1)).findByGroupId(eq(groupId), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(10);
        // 정렬: createdAt DESC, id DESC
        Sort.Order first = used.getSort().getOrderFor("createdAt");
        Sort.Order second = used.getSort().getOrderFor("id");
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(second.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("그룹 + 타입 목록 조회 - type 지정 시 findByGroupIdAndType 호출")
    void getPostListByGroup_withType_filtersByType() {
        // given
        Long groupId = 2L;
        PostType type = PostType.ANNOUNCEMENT;
        LocalDateTime now = LocalDateTime.now();

        Post p1 = buildPost(21L, groupId, 7L, type, "공지 X", "내용 X", now.minusDays(1), now.minusDays(1));
        Page<Post> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))), 1);
        when(postRepository.findByGroupIdAndType(eq(groupId), eq(type), any(Pageable.class))).thenReturn(page);

        // when
        PostListResponse<PostSummaryResponse> res = postService.getPostList(groupId, 0, 5, type);

        // then
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getType()).isEqualTo(PostType.ANNOUNCEMENT);
        assertThat(res.getTotalElements()).isEqualTo(1);

        verify(postRepository, times(1)).findByGroupIdAndType(eq(groupId), eq(type), any(Pageable.class));
        verify(postRepository, never()).findByGroupId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지/사이즈 null 또는 잘못된 값 보정 - 기본값 page=0,size=10 및 최대 100 제한")
    void getPostListByGroup_pageSizeDefaultsAndCaps() {
        // given
        Long groupId = 3L;
        // 반환 내용은 중요치 않으므로 빈 페이지로 대체
        Page<Post> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))), 0);
        when(postRepository.findByGroupId(eq(groupId), any(Pageable.class))).thenReturn(empty);

        // when: page=null, size=null → 0,10으로 보정
        postService.getPostList(groupId, null, null, null);

        // then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findByGroupId(eq(groupId), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(10);

        // when: page=-1, size=1000 → 0, 100(상한)으로 보정
        reset(postRepository);
        when(postRepository.findByGroupId(eq(groupId), any(Pageable.class))).thenReturn(empty);
        postService.getPostList(groupId, -1, 1000, null);

        verify(postRepository).findByGroupId(eq(groupId), pageableCaptor.capture());
        Pageable used2 = pageableCaptor.getValue();
        assertThat(used2.getPageNumber()).isEqualTo(0);
        assertThat(used2.getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("게시글 수정 - null 필드는 유지, 지정 필드만 변경")
    void update_success_partial() {
        // given: BaseEntity 필드는 Reflection으로 주입, status는 Enum 사용
        Post origin = buildPost(
                100L,
                1L,
                5L,
                PostType.ANNOUNCEMENT,
                "old-title",
                "old-content",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                PostStatus.ACTIVE,
                0L
        );

        when(postRepository.findByIdAndStatus(100L, PostStatus.ACTIVE)).thenReturn(Optional.of(origin));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostUpdateRequest req = new PostUpdateRequest();
        req.setTitle("new-title");          // 바꾸기
        req.setContent(null);               // 유지
        req.setType(PostType.FREE);         // 바꾸기

        // when
        PostResponse res = postService.update(100L, req);

        // then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("new-title");
        assertThat(saved.getContent()).isEqualTo("old-content");
        assertThat(saved.getType()).isEqualTo(PostType.FREE);
        assertThat(res.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("게시글 수정 - 존재하지 않거나 삭제된 경우 404(예외)")
    void update_notFound_or_deleted() {
        // 서비스는 ACTIVE 기준으로 조회 → 빈 값 리턴하면 404 예외 발생
        when(postRepository.findByIdAndStatus(100L, PostStatus.ACTIVE)).thenReturn(Optional.empty());

        PostUpdateRequest req = new PostUpdateRequest();
        req.setTitle("x");

        assertThatThrownBy(() -> postService.update(100L, req))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("삭제 성공 - ACTIVE 글을 DELETED로 전환")
    void delete_success() {
        // given
        Post post = Post.builder()
                .groupId(1L)
                .memberId(5L)
                .type(PostType.FREE)
                .title("t")
                .content("c")
                .status(PostStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(post, "id", 100L);
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now().minusHours(1));

        when(postRepository.findByIdAndStatus(100L, PostStatus.ACTIVE)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        postService.deletePost(100L);

        // then
        verify(postRepository).save(argThat(p -> p.getStatus() == PostStatus.DELETED));
    }

    @Test
    @DisplayName("삭제 실패 - 대상 없음 또는 이미 삭제됨 → 404 매핑용 예외")
    void delete_notFound_or_alreadyDeleted() {
        when(postRepository.findByIdAndStatus(999L, PostStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // -------------------------
    // 테스트용 헬퍼 (오버로드 포함)
    // -------------------------

    // 가장 자주 쓰는 형태(기본 status=ACTIVE, likeCount=0)
    private Post buildPost(Long id, Long groupId,
                           Long memberId, PostType type,
                           String title, String content,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt
    ) {
        return buildPost(id, groupId, memberId, type, title, content, createdAt, updatedAt, PostStatus.ACTIVE, 0L);
    }

    // 전체 필드 지정 버전
    private Post buildPost(Long id, Long groupId,
                           Long memberId, PostType type,
                           String title, String content,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           PostStatus status,
                           Long likeCount
    ) {
        Post post = Post.builder()
                .groupId(groupId)
                .memberId(memberId)
                .type(type)
                .title(title)
                .content(content)
                .status(status != null ? status : PostStatus.ACTIVE)
                .likeCount(likeCount != null ? likeCount : 0L)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        ReflectionTestUtils.setField(post, "updatedAt", updatedAt);
        return post;
    }
}