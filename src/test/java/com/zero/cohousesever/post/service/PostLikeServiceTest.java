package com.zero.cohousesever.post.service;

import com.zero.cohousesever.post.dto.postLike.*;
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
        // given
        Long postId = 10L;
        Long memberId = 7L;

        when(postLikeRepository.existsByPostIdAndMemberId(postId, memberId))
                .thenReturn(false);
        when(postLikeRepository.saveAndFlush(any(PostLike.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(postLikeRepository.countByPostId(postId))
                .thenReturn(1L);

        // when
        PostLikeToggleResponse res = postLikeService.updateLikeStatus(postId, memberId);

        // then
        assertThat(res.isLiked()).isTrue();
        assertThat(res.getLikeCount()).isEqualTo(1L);

        verify(postLikeRepository).existsByPostIdAndMemberId(postId, memberId);
        verify(postLikeRepository).saveAndFlush(any(PostLike.class)); // ✅ save가 아니라 saveAndFlush
        verify(postLikeRepository).countByPostId(postId);
        verifyNoMoreInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("존재O → 삭제(해제) → isLiked=false & 최신 count 반환")
    void toggle_delete_unlike_then_count() {
        // given
        Long postId = 10L;
        Long memberId = 7L;

        when(postLikeRepository.existsByPostIdAndMemberId(postId, memberId))
                .thenReturn(true);
        when(postLikeRepository.countByPostId(postId))
                .thenReturn(0L);

        // when
        PostLikeToggleResponse res = postLikeService.updateLikeStatus(postId, memberId);

        // then
        assertThat(res.isLiked()).isFalse();
        assertThat(res.getLikeCount()).isEqualTo(0L);

        verify(postLikeRepository).existsByPostIdAndMemberId(postId, memberId);
        verify(postLikeRepository).deleteByPostIdAndMemberId(postId, memberId);
        verify(postLikeRepository).countByPostId(postId);
        verifyNoMoreInteractions(postLikeRepository);
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

    @Test
    @DisplayName("좋아요한 사용자 목록 - 비어 있을 때 totalCount=0 & likers 빈 배열")
    void getLikers_returnsEmptyList_whenNoLikes() {
        Long postId = 100L;
        when(postLikeRepository.findLikerDtosByPostIdOrderByCreatedDesc(postId))
                .thenReturn(java.util.List.of());

        PostLikeListResponse res = postLikeService.getLikers(postId);

        assertThat(res.getPostId()).isEqualTo(postId);
        assertThat(res.getTotalCount()).isEqualTo(0);
        assertThat(res.getLikers()).isEmpty();

        verify(postLikeRepository, times(1)).findLikerDtosByPostIdOrderByCreatedDesc(postId);
        verifyNoMoreInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("좋아요한 사용자 목록 - 존재할 때 memberId/이름/아바타가 순서대로 매핑")
    void getLikers_returnsDtos_inCreatedDescOrder() {
        Long postId = 200L;

        var d1 = new PostLikerDto(3L, "그룹원3", "/img3.png");
        var d2 = new PostLikerDto(2L, "그룹원2", "/img2.png");
        var d3 = new PostLikerDto(1L, "그룹원1", "/img1.png");
        when(postLikeRepository.findLikerDtosByPostIdOrderByCreatedDesc(postId))
                .thenReturn(java.util.List.of(d1, d2, d3));

        PostLikeListResponse res = postLikeService.getLikers(postId);

        assertThat(res.getPostId()).isEqualTo(postId);
        assertThat(res.getTotalCount()).isEqualTo(3);
        assertThat(res.getLikers()).extracting(PostLikerDto::getMemberId)
                .containsExactly(3L, 2L, 1L);
        assertThat(res.getLikers()).extracting(PostLikerDto::getDisplayName)
                .containsExactly("그룹원3", "그룹원2", "그룹원1");
        assertThat(res.getLikers()).extracting(PostLikerDto::getAvatarUrl)
                .containsExactly("/img3.png", "/img2.png", "/img1.png");

        verify(postLikeRepository, times(1)).findLikerDtosByPostIdOrderByCreatedDesc(postId);
        verifyNoMoreInteractions(postLikeRepository);
    }
}