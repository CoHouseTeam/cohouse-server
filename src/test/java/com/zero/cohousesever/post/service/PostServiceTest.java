package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostRequest;
import com.zero.cohousesever.post.dto.PostResponse;
import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.repository.PostRepository;
import com.zero.cohousesever.post.type.PostType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

        Post saved = Post.builder()
                .groupId(req.getGroupId())
                .memberId(req.getMemberId())
                .type(req.getType())
                .title(req.getTitle())
                .content(req.getContent())
                .build();
        ReflectionTestUtils.setField(saved, "id", 100L);
        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(saved, "updatedAt", LocalDateTime.now());

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
        Post post = Post.builder()
                .groupId(1L)
                .memberId(5L)
                .type(PostType.FREE)
                .title("상세제목")
                .content("상세내용")
                .build();
        ReflectionTestUtils.setField(post, "id", 100L);
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now();
        ReflectionTestUtils.setField(post, "createdAt", created);
        ReflectionTestUtils.setField(post, "updatedAt", updated);

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
}