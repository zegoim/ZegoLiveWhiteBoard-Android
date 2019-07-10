package com.zego.whiteboardedu.manager.entity;

import com.zego.zegoavkit2.error.ZegoError;

public final class ResultCode {

    public final static ResultCode RESULT_CODE_SUCCESS = new ResultCode(ZegoError.kOK, "success");

    private int mCode;
    private String mMsg;

    public ResultCode(int code, String msg) {
        this.mCode = code;
        this.mMsg = msg;
    }

    /**
     * @return 返回结果 是否成功
     */
    public boolean isSuccess() {
        return this.mCode == ZegoError.kOK;
    }

    /**
     * @return 返回错误码
     */
    public int getCode() {
        return this.mCode;
    }


    /**
     * @return 返回错误码对应等信息
     */
    public String getMsg() {
        return this.mMsg;
    }

    @Override
    public String toString() {
        return "ResultCode{" +
                "mCode=" + mCode +
                ", mMsg='" + mMsg + '\'' +
                '}';
    }
}
