package com.kuopan.Entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionWebUserDto {
    private String nickName;
    private String userId;
    private Boolean isAdmin;
}
