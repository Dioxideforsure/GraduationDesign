package com.kuopan.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    @Value("${spring.mail.username}")
    private String sendUserName;

    @Value("${admin.emails}")
    private String adminEmail;

    @Value("${project.folder}")
    private String projectFolder;

    // 手写 Getter，彻底解决 Lombok 不生效导致找不到方法的报错
    public String getSendUserName() {
        return sendUserName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getProjectFolder() {
        return projectFolder;
    }
}