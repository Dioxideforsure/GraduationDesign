package com.kuopan.Exception;

import com.kuopan.Entity.enums.ResponseCodeEnum;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String status;

    public BusinessException() {
        super(ResponseCodeEnum.CODE_500.getMessage());
        status = "error";
        code = ResponseCodeEnum.CODE_500.getCode();
    }

    public BusinessException(String msg) {
        super(msg);
        status = "error";
        code = ResponseCodeEnum.CODE_500.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        status = "error";
        this.code = code;
    }

    public BusinessException(Integer code, String status ,String msg) {
        super(msg);
        this.status = status;
        this.code = code;
    }

    public BusinessException(ResponseCodeEnum r) {
        super(r.getMessage());
        status = "error";
        this.code = r.getCode();
    }


}
