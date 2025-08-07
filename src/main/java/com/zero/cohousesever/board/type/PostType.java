package com.zero.cohousesever.board.type;

import lombok.Getter;

@Getter
public enum PostType {

    ANNOUNCEMENT("공지사항"),
    FREE("자유게시판"),
    ANNIVERSARY("기념일");

    private final String description;

    PostType(String description) {
        this.description = description;
    }
}