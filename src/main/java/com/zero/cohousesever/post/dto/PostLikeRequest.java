package com.zero.cohousesever.post.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeRequest {

    private Long memberId;
    private boolean isLiked;
}