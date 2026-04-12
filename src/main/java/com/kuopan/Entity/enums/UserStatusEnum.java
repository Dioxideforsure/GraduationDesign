package com.kuopan.Entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatusEnum {
    ENABLE(false),
    DISABLE(true);

    private final Boolean status;
}
