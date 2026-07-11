package com.phoenix.tools.vo;

import cn.hutool.core.util.StrUtil;
import com.phoenix.tools.common.ReturnCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 返回vo
 *
 * @Title:
 * @Description:
 * @Author:bruce.liu
 * @Since:2021年04月23日 下午5:46:41
 * @Version:1.1.0
 */
@Data
public class ReturnVo<T> implements Serializable {
    private static final long serialVersionUID = -5580228202640516960L;
    // 响应编码
    private String code;
    // 响应消息
    private String msg;
    // 返回的vo
    private T data;

    public boolean isSuccess() {
        if (StrUtil.isNotBlank(code) && ReturnCode.SUCCESS.equals(code)) {
            return true;
        }
        return false;
    }

    public ReturnVo() {
        super();
    }

    public ReturnVo(String code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public ReturnVo(String code, String msg, T data) {
        super();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private static <T> ReturnVo<T> restResult(T data, String code, String msg) {
        return new ReturnVo<>(code, msg, data);
    }

    /**
     * 查询成功并返回结果集
     *
     * @param <T>
     * @return
     */
    public static <T> ReturnVo<T> ok() {
        return restResult(null, ReturnCode.SUCCESS,"操作成功!");
    }

    /**
     * 查询成功并返回结果集
     *
     * @param data data
     * @param <T>
     * @return
     */
    public static <T> ReturnVo<T> ok(T data) {
        return restResult(data, ReturnCode.SUCCESS,"操作成功!");
    }

    /**
     * 操作成功
     *
     * @param msg msg
     * @param <T>
     * @return
     */
    public static <T> ReturnVo<T> ok(String msg) {
        return restResult(null, ReturnCode.SUCCESS, msg);
    }

    /**
     * 查询操作结果返回
     *
     * @param msg  msg
     * @param data data
     * @param <T>
     * @return
     */
    public static <T> ReturnVo<T> ok(String msg, T data) {
        return restResult(data, ReturnCode.SUCCESS, msg);
    }

    /**
     * 操作失败
     *
     * @param msg 失败信息
     * @param <T>
     * @return
     */
    public static <T> ReturnVo<T> fail(String msg) {
        return restResult(null, ReturnCode.FAIL, msg);
    }

    public static <T> ReturnVo<T> fail(T data) {
        return restResult(data, ReturnCode.FAIL,"操作失败!");
    }

    public static <T> ReturnVo<T> fail(String msg, int code) {
        return restResult(null, String.valueOf(code), msg);
    }

    public static <T> ReturnVo<T> warn(String msg) {
        return restResult(null, ReturnCode.WARN, msg);
    }

    public static <T> ReturnVo<T> error(String msg) {
        return restResult(null, ReturnCode.ERROR, msg);
    }
}
