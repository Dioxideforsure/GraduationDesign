package com.kuopan.Service;

import org.springframework.context.annotation.Bean;

public interface EmailCodeService {
    void sendEmailCode(String email, Integer Type);

    void checkCode(String email, String code);
}
