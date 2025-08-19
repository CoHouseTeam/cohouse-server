package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.PostLikeCountResponse;
import com.zero.cohousesever.post.dto.PostLikeStatusResponse;
import com.zero.cohousesever.post.dto.PostLikeToggleResponse;
import com.zero.cohousesever.post.entity.PostLike;
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
    @DisplayName("존재X → 저장(좋아요) → isLiked=true & 최신 count 반환")
    void toggle_create_like_then_count() {
        Long postId = 10L;
        Long memberId = 7L;

        when(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).thenReturn(false);
        when(postLikeRepository.save(any(PostLike.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postLikeRepository.countByPostId(postId)).thenReturn(1L);

        PostLikeToggleResponse res = postLikeService.updateLikeStatus(postId, memberId);

        assertThat(res.isLiked()).isTrue();
        assertThat(res.getLikeCount()).isEqualTo(1L);
        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(postLikeRepository, never()).deleteByPostIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("존재O → 삭제(해제) → isLiked=false & 최신 count 반환")
    void toggle_delete_unlike_then_count() {
        Long postId = 10L;
        Long memberId = 7L;

        when(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).thenReturn(true);
        when(postLikeRepository.countByPostId(postId)).thenReturn(0L);

        PostLikeToggleResponse res = postLikeService.updateLikeStatus(postId, memberId);

        assertThat(res.isLiked()).isFalse();
        assertThat(res.getLikeCount()).isEqualTo(0L);
        verify(postLikeRepository, times(1)).deleteByPostIdAndMemberId(postId, memberId);
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("게시글 좋아요 개수 조회 - count 반환")
    void getLikeCount_returnsCorrectCount() {
        Long postId = 99L;

        when(postLikeRepository.countByPostId(postId)).thenReturn(5L);

        PostLikeCountResponse res = postLikeService.getLikeCount(postId);

        assertThat(res.getPostId()).isEqualTo(postId);
        assertThat(res.getCount()).isEqualTo(5L);

        verify(postLikeRepository, times(1)).countByPostId(postId);
    }

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