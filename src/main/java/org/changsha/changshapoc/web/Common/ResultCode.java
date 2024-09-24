/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package org.changsha.changshapoc.web.Common;

/**
 * @author zhanglei
 * @version $Id: ResultCode.java, v 0.1 2016年10月26日 下午9:38:31 zhanglei Exp $
 */
public enum ResultCode {

    SUCCESS(200, "成功"),

    PERMISSION_DENIED(201, "没有权限"),

    PARAM_ERROR(204, "参数异常"),

    NOT_SUPPORT_THIS_PERIOD(205, "not support this period"),

    PARAM_TOO_LONG(206, "param to long"),

    NO_DATA(207, "no data"),

    NO_PARM(208, "no param"),

    UNIQUE_DUPLICATE(209, "参数重复"),

    TIME_GAP_SHORT(210, "时间间隔太短"),

    SYSTEM_ERROR(999, "系统异常"),

    BIZ_ERROR(304, "业务异常"),

    FILE_TYPE_ERROR(801, "上传文件格式错误"),

    FILE_CONTENT_ERROR(802, "上传文件数据错误"),

    FILE_CONTENT_REPEAT_ERROR(803, "上传文件数据重复"),

    FILE_CONTENT_ADD_COUNT_ERROR(804, "上传文件数据大于500条"),

    FILE_CONTENT_COUNT_ERROR(805, "已存在数据已大于10000条"),

    RECORD_CONTENT_EXIST_ERROR(806, "记录已存在"),

    FILE_CONTENT_TOO_LONG(807, "上传文件数据长度超限！请限制手机型号和别名长度在50字节以内!"),

    CODE_505_ERROR(505, ""),

    CODE_504_ERROR(504, ""),
    CODE_500_ERROR(500, ""),;

    /**
     * code
     */
    private int code;

    /**
     * msg
     */
    private String msg;

    private ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public int getCode() {
        return code;
    }

    /**
     * Setter method for property <tt>code</tt>.
     *
     * @param code value to be assigned to property code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Getter method for property <tt>msg</tt>.
     *
     * @return property value of msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Setter method for property <tt>msg</tt>.
     *
     * @param msg value to be assigned to property msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

}
