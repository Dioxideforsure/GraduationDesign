package com.kuopan.vo;

import com.kuopan.Entity.enums.ResponseCodeEnum;
import com.kuopan.Exception.BusinessException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("success");
        vo.setCode(ResponseCodeEnum.CODE_200.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_200.getMessage());
        vo.setData(data);
        return vo;
    }

    public static <T> ResponseVO<T> error(BusinessException e, T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("error");
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(data);
        return vo;
    }

    public static <T> ResponseVO<T> error(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("error");
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMessage());
        vo.setData(data);
        return vo;
    }

    public static <T> ResponseVO<T> error() {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("error");
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMessage());
        vo.setData(null);
        return vo;
    }



}
