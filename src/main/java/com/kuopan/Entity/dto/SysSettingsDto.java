package com.kuopan.Entity.dto;

import java.io.Serializable;

public class SysSettingsDto implements Serializable {
    private String emailTitle = "KuoPan网盘 - 邮箱验证码";
    private String emailContent = "您好，您的注册验证码是：【%s】，该验证码15分钟内有效。如非本人操作请忽略。";
    private Integer userInitUseSpace = 1024;

    // ======== 手写 Get/Set 确保不出错 ========
    public String getEmailTitle() {
        return emailTitle;
    }

    public void setEmailTitle(String emailTitle) {
        this.emailTitle = emailTitle;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public Integer getUserInitUseSpace() {
        return userInitUseSpace;
    }

    public void setUserInitUseSpace(Integer userInitUseSpace) {
        this.userInitUseSpace = userInitUseSpace;
    }
}