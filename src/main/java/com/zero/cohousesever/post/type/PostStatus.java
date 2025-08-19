package com.zero.cohousesever.post.type;

import lombok.Getter;

@Getter
public enum PostStatus {
    ACTIVE,   // 정상 노출
    DELETED   // 삭제(소프트 딜리트)
}