package com.zego.whiteboardedu.manager.room;

import com.zego.whiteboardedu.manager.entity.ResultCode;
import com.zego.zegoavkit2.error.ZegoError;

public final class ZegoRoomManagerErrorHelper {

    public final static int LOGIN_ROOM_TIME_OUT_ERROR_CODE = -0x11111;
    public final static int PARAM_ERROR_CODE = -0x11112;

    static ResultCode createRoomResultCode(int roomResultCode) {
        String msg;
        switch (roomResultCode) {
            case LOGIN_ROOM_TIME_OUT_ERROR_CODE: msg = "登录重试超时"; break;
            case PARAM_ERROR_CODE: msg = "参数非法"; break;
            case ZegoError.kConfigServerCouldntConnectError: msg = "无法连接配置服务器，请检查网络是否正常"; break;
            case ZegoError.kConfigServerTimeoutError: msg = "连接配置服务器超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomCouldntConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomTimeoutError: msg = "连接房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomAddUserError: msg = "房间服务器报错，添加用户失败，请联系 ZEGO 技术支持解决（设置了观众无法创建房间后，以观众身份创建房间）"; break;
            case ZegoError.kRoomDecodeSignError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kRoomDoLoginReqError: msg = "发送 login 到房间服务器失败，请检查网络是否正常"; break;
            case ZegoError.kRoomTimeoutError: msg = "登录房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kRoomHbTimeoutError: msg = "房间服务器心跳超时，请检查网络是否正常"; break;
            case ZegoError.kRoomStartConnectError: msg = "与房间服务器连接失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomStatusRspError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            default: msg = "undefined error"; break;
        }
        return new ResultCode(roomResultCode, msg);
    }

    static ResultCode createResultCodeByRoomTempBrokenReason(int tempBrokenReason) {
        String msg;
        switch (tempBrokenReason) {
            default: msg = "connect temp broke"; break;
        }
        return new ResultCode(tempBrokenReason, msg);
    }

    static ResultCode createResultCodeByKickOutReason(int kickOutReason) {
        String msg;
        switch (kickOutReason) {
            case ZegoError.kRoomMultipleLoginKickoutError: msg = "账户多点登录被踢出"; break;
            case ZegoError.kRoomManualKickoutError: msg = "被主动踢出"; break;
            case ZegoError.kLiveRoomSessionError:
            case ZegoError.kRoomSessionErrorKickoutError: msg = "房间会话错误被踢出"; break;
            default: msg = "undefined error"; break;
        }
        return new ResultCode(kickOutReason, msg);
    }

    static ResultCode createResultCodeByRoomDisconnectReason(int disconnectReason) {
        String msg;
        switch (disconnectReason) {
            case ZegoError.kLiveRoomAddUserError: msg = "房间服务器报错，添加用户失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kRoomTimeoutError: msg = "登录房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kRoomHbTimeoutError: msg = "房间服务器心跳超时，请检查网络是否正常"; break;
            case ZegoError.kRoomStatusRspError:
            case ZegoError.kRoomDecodeSignError:
            case ZegoError.kRoomLoginSameCreateUserError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            default: msg = "undefined error"; break;
        }

        return new ResultCode(disconnectReason, msg);
    }


    static ResultCode createResultCodeByPublishState(int publishState) {
        String msg;
        switch (publishState) {
            case ZegoError.kNotLoginError: msg = "未登录房间，请检查是否已成功登录房间"; break;
            case ZegoError.kPublishBadNameError: msg = "重复的推流流名"; break;
            case ZegoError.kFormatUrlError: msg = "URL 格式错误，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kNetworkDnsResolveError: msg = "DNS 解析失败，请确认startPublishing 的 flag 是否为0/2，正确的情况下再检查网络是否正常"; break;
            case ZegoError.kDeniedDisableSwitchLineError: msg = "禁止切换线路重推，请调用推流接口重新推流"; break;
            case ZegoError.kEngineNoPublishDataError: msg = "推流无法推出数据，请检查网络是否正常"; break;
            case ZegoError.kEngineConnectServerError: msg = "连接 RTMP 服务器失败，请检查网络是否正常"; break;
            case ZegoError.kEngineServerDisconnectError: msg = "RTMP 服务器断开连接，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpConnectServerError: msg = "连接 RTP 服务器失败，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpHelloTimeoutError: msg = "连接 RTP 服务器超时，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpCreateSessionTimeoutError: msg = "与 RTP 服务器创建 session 超时，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpTimeoutError: msg = "RTP 超时，请检查网络是否正常"; break;
            case ZegoError.kPlayStreamNotExistError: msg = "流不存在，请检查拉取的流是否已推流成功"; break;
            case ZegoError.kMediaServerForbidError: msg = "ZEGO 后台禁止推流，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kMediaServerPublishBadNameError: msg = "推流使用重复的流名"; break;
            case ZegoError.kConfigMediaNetworkNoUrlError: msg = "媒体服务无 URL，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kConfigServerCouldntConnectError: msg = "无法连接配置服务器，请检查网络是否正常"; break;
            case ZegoError.kConfigServerTimeoutError: msg = "连接配置服务器超时，请检查网络是否正常"; break;
            case ZegoError.kDispatchServerCouldntConnectError: msg = "无法连接调度服务器，请检查网络是否正常"; break;
            case ZegoError.kDispatchServerTimeoutError: msg = "连接调度服务器超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomRequestParamError: msg = "房间参数错误，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kLiveRoomHBTimeoutError: msg = "房间心跳超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomNoPushServerAddrError: msg = "未找到推流服务地址，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kLiveRoomCouldntConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomTimeoutError: msg = "连接房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomRoomAuthError: msg = "房间服务器报错，鉴权失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kLiveRoomNotLoginError: msg = "房间服务器报错，未登录房间，请检查是否已成功登录房间"; break;
            case ZegoError.kLiveRoomAddUserError: msg = "房间服务器报错，添加用户失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kLiveRoomNetBrokenTimeoutError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kLiveRoomPublishBadNameError: msg = "房间服务器报错，重复的推流流名"; break;
            case ZegoError.kRoomConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kRoomTimeoutError: msg = "登录房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kRoomHbTimeoutError: msg = "房间服务器心跳超时，请检查网络是否正常"; break;
            case ZegoError.kRoomDecodeSignError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomLoginCreateUserError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomStatusRspError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomMultipleLoginKickoutError: msg = "重复登录房间被踢，请检查是否已登录房间"; break;
            default: msg = "undefined error"; break;
        }
        return new ResultCode(publishState, msg);
    }

    static ResultCode createResultCodeByPlayState(int playState) {
        String msg;
        switch (playState) {
            case ZegoError.kNetworkDnsResolveError: msg = "DNS 解析失败，请检查网络是否正常"; break;
            case ZegoError.kEngineNoPlayDataError: msg = "拉流无法拉到数据，请检查拉的流是否存在或者网络是否正常"; break;
            case ZegoError.kEngineConnectServerError: msg = "连接 RTMP 服务器失败，请检查网络是否正常"; break;
            case ZegoError.kEngineRtmpHandshakeError: msg = "RTMP 服务连接握手失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kEngineRtmpAppConnectError: msg = "连接 RTMP 服务器失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kEngineServerDisconnectError: msg = "RTMP 服务器断开连接，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpConnectServerError: msg = "连接 RTP 服务器失败，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpHelloTimeoutError: msg = "连接 RTP 服务器超时，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpCreateSessionTimeoutError: msg = "与 RTP 服务器创建 session 超时，请检查网络是否正常"; break;
            case ZegoError.kEngineRtpTimeoutError: msg = "RTP 超时，请检查网络是否正常"; break;
            case ZegoError.kEngineHttpFlvServerDisconnectError: msg = "http flv 服务器断开连接，请检查网络是否正常"; break;
            case ZegoError.kPlayStreamNotExistError: msg = "拉的流不存在，请检查拉取的流是否已推流成功"; break;
            case ZegoError.kMediaServerForbidError: msg = "ZEGO 后台禁止推流，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kConfigServerCouldntConnectError: msg = "无法连接配置服务器，请检查网络是否正常"; break;
            case ZegoError.kConfigServerTimeoutError: msg = "连接配置服务器超时，请检查网络是否正常"; break;
            case ZegoError.kDispatchStreamNotExistError: msg = "调度服务器报错，流不存在，请检查拉取的流是否已推流成功"; break;
            case ZegoError.kLiveRoomHBTimeoutError: msg = "房间心跳超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomCouldntConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomTimeoutError: msg = "连接房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kLiveRoomNotLoginError: msg = "房间服务器报错，未登录房间，请检查是否已成功登录房间"; break;
            case ZegoError.kLiveRoomSessionError: msg = "房间服务器报错，Session错误，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kLiveRoomAddUserError: msg = "房间服务器报错，添加用户失败，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomConnectError: msg = "无法连接房间服务器，请检查网络是否正常"; break;
            case ZegoError.kRoomTimeoutError: msg = "登录房间服务器超时，请检查网络是否正常"; break;
            case ZegoError.kRoomHbTimeoutError: msg = "房间服务器心跳超时,请检查网络是否正常"; break;
            case ZegoError.kRoomDecodeSignError: msg = "房间服务器报错，请联系 ZEGO 技术支持解决"; break;
            case ZegoError.kRoomMultipleLoginKickoutError: msg = "重复登录房间被踢，请检查是否已登录房间"; break;
            default: msg = "undefined error"; break;
        }
        return new ResultCode(playState, msg);
    }


    static ResultCode createResultCodeByLiveEventTempBrokenReason() {
        String msg = "流临时断开，SDK将自动重试";
        return new ResultCode(7, msg);
    }

    static ResultCode createResultCodeBySendCmdErrorCode(int sendCmdErrorCode) {
        String msg;
        if (ZegoError.isNotLoginError(sendCmdErrorCode)) {
            msg = "未登录就发送信令消息";
        } else {
            switch (sendCmdErrorCode) {
                case ZegoError.kNotLoginError: msg = "not login"; break;
                case PARAM_ERROR_CODE: msg = "param invalid"; break;
                default: msg = "network error"; break;
            }
        }
        return new ResultCode(sendCmdErrorCode, msg);
    }
}
