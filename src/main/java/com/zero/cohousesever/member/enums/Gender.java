package com.zero.cohousesever.member.enums;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum Gender {
    MALE(0, "남자"),
    FEMALE(1, "여자");

    private final int code;
    private final String description;

    Gender(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Gender fromValue(int value) {
        for (Gender e : Gender.values()) {
            if (e.code == value) {
                return e;
            }
        }

        throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    public static Gender fromName(String name) {
        for (Gender e : Gender.values()) {
            if (e.description.equals(name)) {
                return e;
            }
        }

        throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
}
