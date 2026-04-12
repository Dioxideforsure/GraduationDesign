package com.kuopan.Entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto implements Serializable {
    private String EmailTitle = "邮箱验证码";

    private String EmailContent = "您好，您的邮箱验证码是：%s，8分钟有效";

    private Integer userInitialSpace = 5;
}
