package com.kuopan.Exception;

import com.kuopan.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseVO<?> handleBusinessException(BusinessException e) {
        ResponseVO<Object> vo = new ResponseVO<>();
        vo.setCode(e.getCode());
        vo.setStatus(e.getStatus());
        vo.setInfo(e.getMessage());
        vo.setData(null);
        log.warn("Business Exception: Reason:{}", e.getMessage());
        return vo;
    }

    @ExceptionHandler(Exception.class)
    public ResponseVO<?> handleException(Exception e) {
        log.error("Crash!! Reason:{}", e.getMessage());
        ResponseVO<Object> vo = new ResponseVO<>();
        vo.setStatus("error");
        vo.setCode(500);
        vo.setInfo("Internal Server Error");
        return vo;
    }
}
