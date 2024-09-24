/**
 * Aplipay.com Inc.
 * Copyright (c) 2004-YEARALL Rights Reserved.
 */
package org.changsha.changshapoc.web.Common;
import lombok.Data;

@Data
public class ResponseResult<T> {

    private int code;
    private String msg;
    private T data;

    private ResponseResult() {
    }

    public static <T> ResponseResult<T> error(ResultCode resultCode) {
        ResponseResult<T> r = new ResponseResult<T>();
        r.setCode(resultCode.getCode());
        r.setMsg(resultCode.getMsg());
        return r;
    }

    public static <T> ResponseResult<T> error(Integer resultCode, String msg) {
        ResponseResult<T> r = new ResponseResult<T>();
        r.setCode(resultCode);
        r.setMsg(msg);
        return r;
    }


    public static <T> ResponseResult<T> success() {
        ResponseResult<T> r = new ResponseResult<T>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMsg());
        return r;
    }

    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> r = new ResponseResult<T>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    public static <T> ResponseResult<T> success(T data,String msg) {
        ResponseResult<T> r = new ResponseResult<T>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
