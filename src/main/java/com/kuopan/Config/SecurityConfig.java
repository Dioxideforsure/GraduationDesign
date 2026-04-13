package com.kuopan.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/file_res/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(
                        "/checkCode", "/sendEmailCode", "/resetPassword", "/login",
                        "/getUsedSpace", "/updatePassword", "/exit", "/file/upload",
                        "/file/list", "/file/uploadChunk", "/file/mergeChunk", "/file/checkMd5",
                        "/file/newFolder", "/file/delFile",
                        "/recycle/**"
                )
                .permitAll()
                .anyRequest().authenticated()
                .and().csrf().disable()
                .headers().frameOptions().sameOrigin();

        return http.build();
    }
}