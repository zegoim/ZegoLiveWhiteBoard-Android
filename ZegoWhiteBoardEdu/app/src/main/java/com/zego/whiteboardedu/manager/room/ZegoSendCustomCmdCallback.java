package com.zego.whiteboardedu.manager.room;

import com.zego.whiteboardedu.manager.entity.ResultCode;

/**
 * 发送 信令 回调代理
 */
public interface ZegoSendCustomCmdCallback {

    /**
     * 发送 信令 回调方法
     *
     * @param resultCode 发送结果，当 {@link ResultCode#isSuccess()} 为 true 时，发送成功，否则，发送失败。
     */
    void onSendCustomCmd(ResultCode resultCode);
}
