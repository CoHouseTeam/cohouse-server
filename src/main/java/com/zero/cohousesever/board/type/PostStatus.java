package com.zero.cohousesever.board.type;

import lombok.Getter;

@Getter
public enum PostStatus {

    ANNOUNCEMENT("공지사항"),
    ANNIVERSARY("기념일"),
    FREE("자유게시판");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }
}