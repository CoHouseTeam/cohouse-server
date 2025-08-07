package com.zero.cohousesever.board.type;

import lombok.Getter;

@Getter
public enum PostType {

    ANNOUNCEMENT("공지사항"),
    FREE("자유게시판");

    private final String description;

    PostType(String description) {
        this.description = description;
    }
}