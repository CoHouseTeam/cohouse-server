package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 작성 성공 - 정상적인 데이터로 게시글 작성")
    void givenValidRequest_whenCreatePost_thenReturnsId() {
        // given
        PostRequest req = new PostRequest();
        req.setGroupId(10L);
        req.setMemberId(20L);
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

        when(postRepository.save(any(Post.class))).thenReturn(saved);

        // when
        Long id = postService.createPost(req);

        // then
        assertThat(id).isEqualTo(100L);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, times(1)).save(captor.capture());
        Post toSave = captor.getValue();
        assertThat(toSave.getGroupId()).isEqualTo(10L);
        assertThat(toSave.getMemberId()).isEqualTo(20L);
        assertThat(toSave.getType()).isEqualTo(PostType.ANNOUNCEMENT);
        assertThat(toSave.getTitle()).isEqualTo("테스트 제목");
        assertThat(toSave.getContent()).isEqualTo("테스트 내용");
    }
}