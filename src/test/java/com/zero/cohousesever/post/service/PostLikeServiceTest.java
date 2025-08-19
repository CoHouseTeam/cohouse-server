package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostLikeStatusResponse;
import com.zero.cohousesever.post.repository.PostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT) // 불필요 스텁 경고 완화
class PostLikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    @Test
    @DisplayName("좋아요 상태 조회 - 존재할 때 liked=true")
    void getStatus_returnsTrue_whenLikeExists() {
        Long postId = 101L;
        Long memberId = 5L;

        when(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).thenReturn(true);

        PostLikeStatusResponse res = postLikeService.getMyLikeStatus(postId, memberId);

        assertThat(res.getPostId()).isEqualTo(postId);
        assertThat(res.isLiked()).isTrue();
        verify(postLikeRepository, times(1)).existsByPostIdAndMemberId(postId, memberId);
        verifyNoMoreInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("좋아요 상태 조회 - 부재할 때 liked=false")
    void getStatus_returnsFalse_whenLikeNotExists() {
        Long postId = 202L;
        Long memberId = 7L;

        when(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).thenReturn(false);

        PostLikeStatusResponse res = postLikeService.getMyLikeStatus(postId, memberId);

        assertThat(res.getPostId()).isEqualTo(postId);
        assertThat(res.isLiked()).isFalse();
        verify(postLikeRepository, times(1)).existsByPostIdAndMemberId(postId, memberId);
        verifyNoMoreInteractions(postLikeRepository);
    }

}