package com.kuopan.Annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {

    /**
     *  Validate Params
     *
     * @return
     *
     */
    boolean checkParams() default false;

    /**
     * Validate login status
     *
     * @return
     *
     */
    boolean checkLogin() default true;

    /**
     * Validate admin identity
     *
     * @return
     *
     * */
    boolean checkAdmin() default false;

}
