package com.tqmall.common;

/**
 * Created by Minutch on 2015-05-31.
 */

/**
 * Ajax处理结果对象
 */
public class Result<T> {

    /**
     * 正确返回这里为空null，否则为错误信息
     */
    private String errorMsg;

    /**
     * 错误码code
     */
    private String code;
    /**
     * 返回的数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    public Result() {
    }

    public Result(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static <T> Result<T> wrapSuccessfulResult(T data) {
        Result<T> result = new Result<T>();
        result.data = data;
        result.success = true;
        return result;
    }

    public static <T> Result<T> wrapErrorResult(String code, String message) {
        Result<T> result = new Result<T>();
        result.success = false;
        result.code = code;
        result.errorMsg = message;
        return result;
    }
}