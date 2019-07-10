package com.zego.whiteboardedu.manager.room;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.zego.whiteboardedu.manager.entity.ResultCode;
import com.zego.whiteboardedu.manager.entity.ZegoUser;
import com.zego.whiteboardedu.manager.utils.MD5Utils;
import com.zego.whiteboardedu.manager.utils.ZegoStreamInfoHelper;
import com.zego.zegoavkit2.error.ZegoError;
import com.zego.zegoavkit2.soundlevel.IZegoSoundLevelCallback;
import com.zego.zegoavkit2.soundlevel.ZegoSoundLevelInfo;
import com.zego.zegoavkit2.soundlevel.ZegoSoundLevelMonitor;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoCustomCommandCallback;
import com.zego.zegoliveroom.callback.IZegoLiveEventCallback;
import com.zego.zegoliveroom.callback.IZegoLivePlayerCallback;
import com.zego.zegoliveroom.callback.IZegoLivePublisherCallback;
import com.zego.zegoliveroom.callback.IZegoLoginCompletionCallback;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.callback.ZegoLiveEventCallbackConstants;
import com.zego.zegoliveroom.constants.ZegoConstants;
import com.zego.zegoliveroom.constants.ZegoVideoViewMode;
import com.zego.zegoliveroom.entity.AuxData;
import com.zego.zegoliveroom.entity.ZegoPlayStreamQuality;
import com.zego.zegoliveroom.entity.ZegoPublishStreamQuality;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public final class ZegoRoomManager implements IZegoRoomCallback, IZegoLiveEventCallback, IZegoLivePublisherCallback, IZegoLivePlayerCallback, IZegoSoundLevelCallback {

    private final static String TAG = ZegoRoomManager.class.getSimpleName();

    private final static String LIVE_EVENT_STREAM_KEY = "StreamID";

    private final static int RECONNECT_ROOM_MESSAGE = 0x1;
    private final static int START_USER_LIVE_MESSAGE = 0x2;


    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECONNECT_ROOM_MESSAGE:
                    Log.d(TAG, "mUIHandler 重连房间");
                    reconnectRoom();
                    break;
                case START_USER_LIVE_MESSAGE:
                    Log.d(TAG, "mUIHandler 重新推拉流 user" + msg.obj);
                    startUserLive((ZegoUser) msg.obj);
                    break;
            }
        }
    };

    private ZegoLiveRoom mZegoLiveRoom;

    /**
     * 是否纯音频推拉流
     */
    private boolean isOnlyAudio;

    /**
     * 是否已经开启预览，默认为false
     */
    private boolean isPreview;

    /**
     * 预览视图
     */
    private View mPreviewView;

    /**
     * 设置预览视图裁剪模式
     */
    private int mPreviewViewMode;

    private boolean isCaptureFirstFrame;

    private ZegoRoomInfo mRoomInfo;

    private ZegoUser mUserInfo;

    private boolean isDisconnect;

    /**
     * 详情看 {@link ZegoRoomLoginStatus}
     */
    private int mLoginStatus;

    /**
     * 断线自动重连房间，默认为false
     */
    private boolean isAutoReconnectRoom;

    /**
     * <p>断线自动重连尝试超时时间，默认0秒，不会超时</p>
     * <p>当 {@link #isAutoReconnectRoom} 为true的时候，该值才有效果</p>
     */
    private int mReconnectTimeoutSec;

    /**
     * 房间断线重连、推拉流失败重试操作间隔 单位：毫秒  默认值 1000毫秒
     */
    private long mReconnectInterval;

    private Timer mStopReconnectTimer;

    private ZegoLoginRoomCallback mZegoLoginRoomCallback;

    /**
     * 是否开启声浪监听
     */
    private boolean isSoundLevelMonitor;

    /**
     * 声浪监听时间 单位：毫秒  默认值 500毫秒
     */
    private int mSoundLevelMonitorCycle;


    private Set<ZegoStreamInfo> mAllStreams;  // 不包含当前用户

    private Map<ZegoUser, ZegoUserLiveInfo> mLiveInfos;

    private List<ZegoRoomManagerCallback> mManagerCallbacks;

    private List<ZegoRoomManagerLiveStatusCallback> mLiveStatusCallbacks;

    private ZegoRoomManagerIDGenerateCallback mIDGenerateCallback;

    private ZegoRoomManager() {

    }

    public static ZegoRoomManager managerWithLiveRoom(ZegoLiveRoom zegoLiveRoom, boolean isOnlyAudio) {

        ZegoRoomManager instance = new ZegoRoomManager();
        instance.isOnlyAudio = isOnlyAudio;
        instance.isPreview = false;
        instance.mAllStreams = new HashSet<>();
        instance.mLiveInfos = new HashMap<>();
        instance.mReconnectInterval = 1000;
        instance.mSoundLevelMonitorCycle = 500;
        instance.mManagerCallbacks = new ArrayList<>();
        instance.mLiveStatusCallbacks = new ArrayList<>();

        instance.mPreviewView = null;
        instance.mPreviewViewMode = ZegoVideoViewMode.ScaleAspectFill;
        instance.isCaptureFirstFrame = false;

        instance.mZegoLiveRoom = zegoLiveRoom;
        instance.mZegoLiveRoom.enableCamera(!isOnlyAudio);
        instance.setZegoLiveRoomCallback();

        return instance;
    }

    public void joinRoom(final ZegoRoomInfo roomInfo, ZegoUser userInfo, final ZegoLoginRoomCallback loginRoomCallback) {
        if (mLoginStatus != ZegoRoomLoginStatus.LOGOUT) {
            Log.w(TAG, "joinRoom mLoginStatus = " + mLoginStatus);
            return;
        }

        mRoomInfo = roomInfo;

        mUserInfo = userInfo;
        ZegoLiveRoom.setUser(userInfo.userID, userInfo.userName);

        int role = mUserInfo.equals(roomInfo.mOwner) ? ZegoConstants.RoomRole.Anchor : ZegoConstants.RoomRole.Audience;

        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.LOGIN, ResultCode.RESULT_CODE_SUCCESS);

        boolean result = mZegoLiveRoom.loginRoom(roomInfo.mRoomID, roomInfo.mRoomName, role, new IZegoLoginCompletionCallback() {
            @Override
            public void onLoginCompletion(int errorCode, ZegoStreamInfo[] streamList) {
                onLoginRoomComplete(errorCode, streamList, roomInfo, loginRoomCallback);
            }
        });

        if (result) {
            mZegoLoginRoomCallback = loginRoomCallback;
            if (isAutoReconnectRoom) {
                setupTimeoutTimer();
            }
        } else {
            ResultCode resultCode = ZegoRoomManagerErrorHelper.createRoomResultCode(ZegoRoomManagerErrorHelper.PARAM_ERROR_CODE);
            updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.LOGIN, resultCode);

            if (isAutoReconnectRoom) {
                endLoginRetryWithReason(ZegoRoomReconnectStopReason.Param);
            }
        }
    }

    public void leaveRoom() {

        removeAllLiveUsers();

        leaveRoomInner();

        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.LOGOUT, ResultCode.RESULT_CODE_SUCCESS);

        if (isAutoReconnectRoom && !isLogin()) {
            endLoginRetryWithReason(ZegoRoomReconnectStopReason.LOGOUT);
        }

        reset();
    }

    private void reset() {
        mZegoLoginRoomCallback = null;
        mRoomInfo = null;
        isDisconnect = false;
        mLiveInfos.clear();
        mAllStreams.clear();
    }

    public void release() {
        // 先执行 leaveRoom, 是否跟指定房间绑定的变量
        leaveRoom();

        // 将回调列表清空
        clearAllCallback();

        // 停止声浪监控，移除回调
        setSoundLevelMonitor(false);

        // 移除 绑定的 ZegoLiveRoom Callback
        removeZegoLiveRoomCallback();

        mIDGenerateCallback = null;

        mZegoLiveRoom = null;
    }

    private void clearAllCallback() {
        mManagerCallbacks.clear();
        mLiveStatusCallbacks.clear();
    }

    public void startLive() {
        addLiveUser(mUserInfo);
    }

    public void stopLive() {
        removeLiveUser(mUserInfo);
    }

    public void startPreview() {
        if (isOnlyAudio || isPreview) {
            return;
        }

        mZegoLiveRoom.setPreviewViewMode(mPreviewViewMode);
        mZegoLiveRoom.setPreviewView(mPreviewView);
        mZegoLiveRoom.startPreview();

        isPreview = true;
    }

    public void stopPreview() {
        if (isOnlyAudio || !isPreview) {
            return;
        }

        mZegoLiveRoom.setPreviewView(null);
        mZegoLiveRoom.stopPreview();

        isPreview = false;

        updateCaptureFirstFrameIfNeed();
    }

    public void setLiveExtraInfo(String extraInfo) {
        mZegoLiveRoom.updateStreamExtraInfo(extraInfo);

        setExtraInfo(extraInfo, mUserInfo);
    }

    public void setVolumeForUser(int volume, ZegoUser user) {
        boolean isLocal = mUserInfo.equals(user);
        if (isLocal) {
            mZegoLiveRoom.setCaptureVolume(volume);
        } else {
            ZegoStreamInfo streamInfo = streamForUser(user);
            if (streamInfo != null) {
                mZegoLiveRoom.setPlayVolume(volume, streamInfo.streamID);
            }
        }
    }

    public void setLiveVideoView(View liveVideoView, int viewMode, ZegoUser forUser) {
        if (isOnlyAudio) {
            return;
        }

        boolean isSelf = forUser == null || mUserInfo.equals(forUser);
        if (isSelf) {
            mPreviewView = liveVideoView;
            mPreviewViewMode = viewMode;

            if (isPreview) {
                mZegoLiveRoom.setPreviewView(liveVideoView);
                mZegoLiveRoom.setPreviewViewMode(viewMode);
            }
            return;
        }

        ZegoUserLiveInfo info = liveInfoForUser(forUser);
        if (info == null) {
            return;
        }

        info.mViewMode = viewMode;
        info.mVideoView = liveVideoView;

        boolean canSetPlayView = info.mFirstFrame;
        if (canSetPlayView) {
            String streamID = streamIDForUser(forUser);
            mZegoLiveRoom.setViewMode(viewMode, streamID);
            mZegoLiveRoom.updatePlayView(streamID, liveVideoView);
        }
    }

    public void sendCustomCommand(String content, ZegoUser[] memberList, final ZegoSendCustomCmdCallback callback) {
        // 转换成真正的ZegoUser对象
        com.zego.zegoliveroom.entity.ZegoUser[] realMemberList = new com.zego.zegoliveroom.entity.ZegoUser[memberList.length];
        for (int i = 0; i < memberList.length; i++) {
            realMemberList[i] = memberList[i].toInnerZegoUser();
        }

        boolean result = mZegoLiveRoom.sendCustomCommand(realMemberList, content, new IZegoCustomCommandCallback() {
            @Override
            public void onSendCustomCommand(int errorCode, String roomID) {
                if (!isCurrentRoom(roomID)) {
                    return;
                }

                boolean isSuccess = errorCode == 0;
                if (isSuccess) {
                    if (callback != null) {
                        callback.onSendCustomCmd(ResultCode.RESULT_CODE_SUCCESS);
                    }
                    return;
                }

                if (callback != null) {
                    ResultCode resultCode = ZegoRoomManagerErrorHelper.createResultCodeBySendCmdErrorCode(errorCode);
                    callback.onSendCustomCmd(resultCode);
                }
            }
        });
        if (!result) {
            if (callback != null) {
                ResultCode resultCode = ZegoRoomManagerErrorHelper.createResultCodeBySendCmdErrorCode(ZegoRoomManagerErrorHelper.PARAM_ERROR_CODE);
                callback.onSendCustomCmd(resultCode);
            }
        }
    }

    public void addManagerCallback(ZegoRoomManagerCallback managerCallback) {
        mManagerCallbacks.add(managerCallback);
    }

    public void removeManagerCallback(ZegoRoomManagerCallback managerCallback) {
        mManagerCallbacks.remove(managerCallback);
    }

    public void addLiveStatusCallback(ZegoRoomManagerLiveStatusCallback liveStatusCallback) {
        mLiveStatusCallbacks.add(liveStatusCallback);
    }

    public void removeLiveStatusCallback(ZegoRoomManagerLiveStatusCallback liveStatusCallback) {
        mLiveStatusCallbacks.remove(liveStatusCallback);
    }

    public void setIDGenerateCallback(ZegoRoomManagerIDGenerateCallback idGenerateCallback) {
        mIDGenerateCallback = idGenerateCallback;
    }

    public void setAutoReconnectRoom(boolean isAutoReconnectRoom) {
        if (this.isAutoReconnectRoom != isAutoReconnectRoom) {
            this.isAutoReconnectRoom = isAutoReconnectRoom;
            mUIHandler.removeCallbacksAndMessages(null);
        }

    }

    public void setReconnectTimeoutSec(int reconnectTimeoutSec) {
        mReconnectTimeoutSec = reconnectTimeoutSec;
    }

    public void setSoundLevelMonitor(boolean isSoundLevelMonitor) {
        if (this.isSoundLevelMonitor == isSoundLevelMonitor) {
            return;
        }
        this.isSoundLevelMonitor = isSoundLevelMonitor;
        updateLiveRoomSoundLevelMonitorState();
    }

    public void setSoundLevelMonitorCycle(int soundLevelMonitorCycle) {
        if (soundLevelMonitorCycle > 3000) {
            soundLevelMonitorCycle = 3000;
        } else if (soundLevelMonitorCycle < 100) {
            soundLevelMonitorCycle = 100;
        }
        mSoundLevelMonitorCycle = soundLevelMonitorCycle;
        ZegoSoundLevelMonitor.getInstance().setCycle(soundLevelMonitorCycle);
    }

    private void setZegoLiveRoomCallback() {
        mZegoLiveRoom.setZegoRoomCallback(this);
        mZegoLiveRoom.setZegoLivePublisherCallback(this);
        mZegoLiveRoom.setZegoLivePlayerCallback(this);
        mZegoLiveRoom.setZegoLiveEventCallback(this);
    }

    private void removeZegoLiveRoomCallback() {
        mZegoLiveRoom.setZegoRoomCallback(null);
        mZegoLiveRoom.setZegoLivePublisherCallback(null);
        mZegoLiveRoom.setZegoLivePlayerCallback(null);
        mZegoLiveRoom.setZegoLiveEventCallback(null);
    }

    private void setupTimeoutTimer() {
        if (mStopReconnectTimer != null || mReconnectTimeoutSec == 0) {
            return;
        }
        mStopReconnectTimer = new Timer();
        mStopReconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onLoginRetryTimeout();
            }
        }, mReconnectTimeoutSec * 1000);
    }

    private void releaseTimeoutTimer() {
        if (mStopReconnectTimer != null) {
            mStopReconnectTimer.cancel();
            mStopReconnectTimer = null;
        }
    }

    private void onLoginRetryTimeout() {
        final ResultCode resultCode = ZegoRoomManagerErrorHelper.createRoomResultCode(ZegoRoomManagerErrorHelper.LOGIN_ROOM_TIME_OUT_ERROR_CODE);
        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.LOGIN_FAILED, resultCode);

        if (mZegoLoginRoomCallback != null) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    mZegoLoginRoomCallback.onLoginRoom(resultCode);
                }
            });
        }

        endLoginRetryWithReason(ZegoRoomReconnectStopReason.TIME_OUT);
    }

    private void endLoginRetryWithReason(int reason) {
        leaveRoomInner();
        releaseTimeoutTimer();
        for (ZegoRoomManagerCallback callback : mManagerCallbacks) {
            callback.onAutoReconnectStop(reason);
        }
    }

    private void setLoginStatus(int loginStatus) {
        mLoginStatus = loginStatus;
        updateLiveRoomSoundLevelMonitorState();
    }

    private void updateLoginStatusWithLoginEvent(int loginEvent, ResultCode resultCode) {
        switch (mLoginStatus) {
            case ZegoRoomLoginStatus.LOGOUT:
                switch (loginEvent) {
                    case ZegoRoomLoginEvent.LOGIN:
                        setLoginStatus(ZegoRoomLoginStatus.START_LOGIN);
                        break;
                    case ZegoRoomLoginEvent.LOGOUT:
                        break;
                    default:
                        Log.w(TAG, "updateLoginStatusWithLoginEvent status error!! mLoginStatus = " + mLoginStatus + " loginEvent = " + loginEvent);
                        break;
                }
                break;
            case ZegoRoomLoginStatus.START_LOGIN:
                switch (loginEvent) {
                    case ZegoRoomLoginEvent.LOGIN_SUCCESS:
                        setLoginStatus(ZegoRoomLoginStatus.LOGIN);
                        break;
                    case ZegoRoomLoginEvent.LOGIN_FAILED:
                    case ZegoRoomLoginEvent.LOGOUT:
                        setLoginStatus(ZegoRoomLoginStatus.LOGOUT);
                        break;
                    default:
                        Log.w(TAG, "updateLoginStatusWithLoginEvent status error!! mLoginStatus = " + mLoginStatus + " loginEvent = " + loginEvent);
                        break;
                }
                break;
            case ZegoRoomLoginStatus.LOGIN:
                switch (loginEvent) {
                    case ZegoRoomLoginEvent.TEMP_BROKE:
                        setLoginStatus(ZegoRoomLoginStatus.TEMP_BROKE);
                        break;
                    case ZegoRoomLoginEvent.DISCONNECT:
                    case ZegoRoomLoginEvent.KICK_OUT:
                    case ZegoRoomLoginEvent.LOGOUT:
                        setLoginStatus(ZegoRoomLoginStatus.LOGOUT);
                        break;
                    default:
                        Log.w(TAG, "updateLoginStatusWithLoginEvent status error!! mLoginStatus = " + mLoginStatus + " loginEvent = " + loginEvent);
                        break;
                }
                break;
            case ZegoRoomLoginStatus.TEMP_BROKE:
                switch (loginEvent) {
                    case ZegoRoomLoginEvent.TEMP_BROKE:
                        break;
                    case ZegoRoomLoginEvent.RECONNECT:
                        setLoginStatus(ZegoRoomLoginStatus.LOGIN);
                        break;
                    case ZegoRoomLoginEvent.DISCONNECT:
                    case ZegoRoomLoginEvent.KICK_OUT:
                    case ZegoRoomLoginEvent.LOGOUT:
                        setLoginStatus(ZegoRoomLoginStatus.LOGOUT);
                        break;
                    default:
                        Log.w(TAG, "updateLoginStatusWithLoginEvent status error!! mLoginStatus = " + mLoginStatus + " loginEvent = " + loginEvent);
                        break;
                }
                break;
        }
        for (ZegoRoomManagerCallback managerCallback : mManagerCallbacks) {
            managerCallback.onLoginEventOccur(loginEvent, mLoginStatus, resultCode);
        }
    }

    /**
     * 离开房间内部实现
     */
    private void leaveRoomInner() {
        mZegoLiveRoom.logoutRoom();
        // 移除所有的延时任务
        mUIHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 登录房间操作回调
     *
     * @param errorCode  结果码
     * @param streamList 登录房间时房间存在的流列表
     */
    private void onLoginRoomComplete(int errorCode, ZegoStreamInfo[] streamList, ZegoRoomInfo roomInfo, ZegoLoginRoomCallback loginRoomCallback) {
        Log.d(TAG, String.format("onLoginRoomComplete errorCode: %1$d, roomID: %2$s, streamList.length: %3$d", errorCode, roomInfo.mRoomID, streamList == null ? 0 : streamList.length));
        if (errorCode == 0) {
            boolean isReconnect = this.isDisconnect;
            isDisconnect = false;
            updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.LOGIN_SUCCESS, ResultCode.RESULT_CODE_SUCCESS);

            if (isReconnect) {
                handleReconnectRoomStreams(streamList);
            } else {
                addStreams(streamList);
            }

            releaseTimeoutTimer();

            if (loginRoomCallback != null) {
                loginRoomCallback.onLoginRoom(ZegoRoomManagerErrorHelper.createRoomResultCode(errorCode));
                mZegoLoginRoomCallback = null;
            }
        } else {
            ResultCode resultCode = ZegoRoomManagerErrorHelper.createRoomResultCode(errorCode);
            updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.LOGIN_FAILED, resultCode);

            boolean isAudienceCantCreateRoom = resultCode.getCode() == ZegoError.kLiveRoomAddUserError;
            boolean isCustomTokenError = resultCode.getCode() == ZegoError.kLiveRoomThirdTokenAuthError;
            if (isAutoReconnectRoom && (isAudienceCantCreateRoom || isCustomTokenError)) {
                endLoginRetryWithReason(ZegoRoomReconnectStopReason.Param);
                leaveRoomInner();
                if (loginRoomCallback != null) {
                    loginRoomCallback.onLoginRoom(resultCode);
                    mZegoLoginRoomCallback = null;
                }
                return;
            }

            if (isAutoReconnectRoom) {
                mUIHandler.sendEmptyMessageDelayed(RECONNECT_ROOM_MESSAGE, mReconnectInterval);
            } else {
                if (loginRoomCallback != null) {
                    loginRoomCallback.onLoginRoom(resultCode);
                }
            }
        }
    }

    private void retryUserLiveWithUser(ZegoUser user) {
        Message message = new Message();
        message.what = START_USER_LIVE_MESSAGE;
        message.obj = user;
        mUIHandler.sendMessageDelayed(message, mReconnectInterval);
    }

    private void reconnectRoom() {
        if (!isAutoReconnectRoom) {
            return;
        }
        joinRoom(mRoomInfo, mUserInfo, this.mZegoLoginRoomCallback);
    }

    private void handleReconnectRoomStreams(ZegoStreamInfo[] streams) {
        boolean isSelfLiveBefore = isSelfLive();

        Set<ZegoStreamInfo> remoteStreams = new HashSet<>(Arrays.asList(streams));
        Set<ZegoStreamInfo> localStreams = new HashSet<>(mAllStreams);
        Set<ZegoStreamInfo> reconnectStreams = new HashSet<>(mAllStreams);

        // 找出需要重连的Stream，之前有，现在也有
        ZegoStreamInfoHelper.retainAllStreamFromCollection(reconnectStreams, remoteStreams);

        // 找出需要删除的Stream
        ZegoStreamInfoHelper.removeAllStreamFromCollection(localStreams, remoteStreams);

        if (isSelfLiveBefore) {
            startUserLive(mUserInfo);
        }

        // 找出需要添加的Stream
        ZegoStreamInfoHelper.removeAllStreamFromCollection(remoteStreams, Arrays.asList(streams));

        reconnectStreams(reconnectStreams.toArray(new ZegoStreamInfo[0]));
        addStreams(remoteStreams.toArray(new ZegoStreamInfo[0]));
        deleteStreams(localStreams.toArray(new ZegoStreamInfo[0]));
    }

    private void reconnectStreams(ZegoStreamInfo[] streams) {
        for (ZegoStreamInfo stream : streams) {
            ZegoUser user = new ZegoUser();
            user.userID = stream.userID;
            user.userName = stream.userName;

            startUserLive(user);
        }
        updateStreamExtraInfo(streams);
    }

    private void addStreams(ZegoStreamInfo[] streamList) {
        if (streamList == null) {
            return;
        }
        ZegoStreamInfoHelper.addAllStreamToSet(mAllStreams, Arrays.asList(streamList));

        for (ZegoStreamInfo streamInfo : streamList) {
            ZegoUser user = new ZegoUser();
            user.userID = streamInfo.userID;
            user.userName = streamInfo.userName;
            addLiveUser(user);
        }

        updateStreamExtraInfo(streamList);
    }

    private void deleteStreams(ZegoStreamInfo[] streams) {
        if (streams == null) {
            return;
        }

        for (ZegoStreamInfo stream : streams) {
            ZegoUser user = new ZegoUser();
            user.userID = stream.userID;
            user.userName = stream.userName;
            removeLiveUser(user);
        }

        ZegoStreamInfoHelper.removeAllStreamFromCollection(mAllStreams, Arrays.asList(streams));
    }

    private void updateStreamExtraInfo(ZegoStreamInfo[] streams) {
        for (ZegoStreamInfo stream : streams) {
            ZegoUser zegoUser = new ZegoUser();
            zegoUser.userID = stream.userID;
            zegoUser.userName = stream.userName;

            ZegoUserLiveInfo liveInfo = liveInfoForUser(zegoUser);

            boolean isExtraInfoUpdate = false;
            if ((liveInfo.mExtraInfo != null && !liveInfo.mExtraInfo.equals(stream.extraInfo)) ||
                    (stream.extraInfo != null && !stream.extraInfo.equals(liveInfo.mExtraInfo))) {
                isExtraInfoUpdate = true;
            }
            if (isExtraInfoUpdate) {
                setExtraInfo(stream.extraInfo, zegoUser);
            }
        }
    }

    private void addLiveUser(ZegoUser user) {
        if (liveUsers().contains(user)) {
            return;
        }
        ZegoUserLiveInfo liveInfo = new ZegoUserLiveInfo();
        mLiveInfos.put(user, liveInfo);

        for (ZegoRoomManagerCallback managerCallback : mManagerCallbacks) {
            managerCallback.onLiveUserJoin(user);
        }

        setStreamStatus(ZegoUserLiveStatus.WAIT_CONNECT, null, user);

        startUserLive(user);
    }

    private void removeLiveUser(ZegoUser user) {
        if (!liveUsers().contains(user)) {
            return;
        }
        mUIHandler.removeMessages(START_USER_LIVE_MESSAGE, user);

        stopUserLive(user);

        mLiveInfos.remove(user);

        for (ZegoRoomManagerCallback managerCallback : mManagerCallbacks) {
            managerCallback.onLiveUserLeave(user);
        }
    }

    private void removeAllLiveUsers() {
        for (ZegoUser user : liveUsers()) {
            stopUserLive(user);
        }
        mLiveInfos.clear();
    }

    private void startUserLive(ZegoUser user) {
        if (!isLogin()) {
            return;
        }

        if (mUserInfo.equals(user)) {
            startPublish();
        } else {
            startPlayStreamWithUser(user);
        }
    }

    private void stopUserLive(ZegoUser user) {
        if (mUserInfo.equals(user)) {
            stopPublish();
        } else {
            stopPlayStreamWithUser(user);
        }
    }

    private void startPublish() {
        String streamID = streamIDForUser(mUserInfo);
        mZegoLiveRoom.startPublishing(streamID, null, ZegoConstants.PublishFlag.JoinPublish);
        setStreamStatus(ZegoUserLiveStatus.CONNECTING, null, mUserInfo);
    }

    private void stopPublish() {
        mZegoLiveRoom.stopPublishing();
        updateCaptureFirstFrameIfNeed();
    }

    private void startPlayStreamWithUser(ZegoUser toUser) {
        String streamID = streamIDForUser(toUser);
        mZegoLiveRoom.startPlayingStream(streamID, null);
        setStreamStatus(ZegoUserLiveStatus.CONNECTING, null, toUser);
    }

    private void stopPlayStreamWithUser(ZegoUser toUser) {
        String streamID = streamIDForUser(toUser);
        mZegoLiveRoom.updatePlayView(streamID, null);
        mZegoLiveRoom.stopPlayingStream(streamID);
    }

    private void callbackAllLiveStreamError() {
        for (ZegoUser user : mLiveInfos.keySet()) {
            String streamID = streamIDForUser(user);
            ZegoUserLiveInfo liveInfo = mLiveInfos.get(user);

            boolean shouldCallbackError = (liveInfo != null && liveInfo.getStreamStatus() != ZegoUserLiveStatus.WAIT_CONNECT);
            if (shouldCallbackError) {
                boolean isSelf = mUserInfo.equals(user);
                if (isSelf) {
                    onPublishStateUpdate(ZegoError.kLiveRoomTimeoutError, streamID, null);
                } else {
                    onPlayStateUpdate(ZegoError.kLiveRoomTimeoutError, streamID);
                }
            }
        }
    }

    private void updateCaptureFirstFrameIfNeed() {
        ZegoUserLiveInfo userLiveInfo = liveInfoForUser(mUserInfo);

        boolean isCapture = isPreview || (userLiveInfo != null && userLiveInfo.getStatus() != ZegoUserLiveStatus.WAIT_CONNECT);

        if (!isCapture) {
            setFirstFrame(false, 0, 0, mUserInfo);
        }
    }

    private void updateLiveRoomSoundLevelMonitorState() {
        boolean enable = isLogin() && isSoundLevelMonitor;
        if (enable) {
            ZegoSoundLevelMonitor.getInstance().start();
            ZegoSoundLevelMonitor.getInstance().setCallback(this);
            ZegoSoundLevelMonitor.getInstance().setCycle(mSoundLevelMonitorCycle);
        } else {
            ZegoSoundLevelMonitor.getInstance().stop();
            ZegoSoundLevelMonitor.getInstance().setCallback(null);
        }
    }

    // ------------------- liveStatus set operation start ------------------- //
    private void setStreamStatus(int streamStatus, ResultCode errorCode, ZegoUser toUser) {
        ZegoUserLiveInfo liveInfo = mLiveInfos.get(toUser);
        if (liveInfo != null && toUser != null) {
            int oldStatus = liveInfo.getStatus();
            liveInfo.setStreamStatus(streamStatus);
            int newStatus = liveInfo.getStatus();
            if (oldStatus != newStatus) {
                for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                    liveStatusCallback.onLiveStatusChange(toUser, newStatus, errorCode);
                }
            }
        }
    }

    private void setLiveStatus(int liveStatus, ResultCode errorCode, ZegoUser toUser) {
        ZegoUserLiveInfo liveInfo = mLiveInfos.get(toUser);
        if (liveInfo != null && toUser != null) {
            int oldStatus = liveInfo.getStatus();
            liveInfo.setLiveStatus(liveStatus);
            int newStatus = liveInfo.getStatus();
            if (oldStatus != newStatus) {
                for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                    liveStatusCallback.onLiveStatusChange(toUser, newStatus, errorCode);
                }
            }
        }
    }

    private void setExtraInfo(String extraInfo, ZegoUser toUser) {
        ZegoUserLiveInfo liveInfo = mLiveInfos.get(toUser);
        if (liveInfo != null && toUser != null) {
            if (!TextUtils.equals(liveInfo.mExtraInfo, extraInfo)) {
                liveInfo.mExtraInfo = extraInfo;
                for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                    liveStatusCallback.onExtraInfoUpdate(toUser, extraInfo);
                }
            }
        }
    }

    private void setSoundLevel(float soundLevel, ZegoUser toUser) {
        ZegoUserLiveInfo liveInfo = mLiveInfos.get(toUser);
        if (liveInfo != null && toUser != null) {
            liveInfo.mSoundLevel = soundLevel;
            for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                liveStatusCallback.onGetSoundLevel(toUser, soundLevel);
            }
        }
    }

    private void setFirstFrame(boolean firstFrame, int width, int height, ZegoUser toUser) {
        boolean isSelf = mUserInfo.equals(toUser);
        if (isSelf) {
            isCaptureFirstFrame = firstFrame;
            if (firstFrame) {
                for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                    liveStatusCallback.onUserGetFirstFrame(toUser, width, height);
                }
            }
            return;
        }

        ZegoUserLiveInfo liveInfo = mLiveInfos.get(toUser);
        if (liveInfo != null && toUser != null) {
            liveInfo.mFirstFrame = firstFrame;
            if (firstFrame) {
                for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                    liveStatusCallback.onUserGetFirstFrame(toUser, width, height);
                }
            }
        }
    }

    private void setUserLiveQuality(ZegoUserLiveQuality quality, ZegoUser toUser) {
        ZegoUserLiveInfo liveInfo = mLiveInfos.get(toUser);
        if (liveInfo != null && toUser != null) {
            liveInfo.mLiveQuality = quality;
            for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                liveStatusCallback.onLiveQualityUpdate(toUser, quality);
            }
        }
    }
    // ------------------- liveStatus set operation end ------------------- //

    private boolean isSelfLive() {
        return liveUsers().contains(mUserInfo);
    }

    private Set<ZegoUser> liveUsers() {
        return mLiveInfos.keySet();
    }

    private ZegoUserLiveInfo liveInfoForUser(ZegoUser user) {
        ZegoUserLiveInfo info = mLiveInfos.get(user);

        boolean isSelf = mUserInfo.equals(user);
        if (isSelf && info != null) {
            info.mFirstFrame = isCaptureFirstFrame;
            info.mVideoView = mPreviewView;
            info.mViewMode = mPreviewViewMode;
        }

        return mLiveInfos.get(user);
    }

    private String streamIDForUser(ZegoUser user) {
        boolean isSelf = mUserInfo.equals(user);
        // 如果不是当前用户，则在流列表中寻找用户对于的流
        if (!isSelf) {
            for (ZegoStreamInfo streamInfo : mAllStreams) {
                if (streamInfo.userID.equals(user.userID)) {
                    return streamInfo.streamID;
                }
            }
        }
        // 当前用户，或者找不到对于的流
        return getLiveIDWithUser(user);
    }

    private String getLiveIDWithUser(ZegoUser user) {
        return mIDGenerateCallback == null ? genLiveIDWithUser(user) : mIDGenerateCallback.genLiveIDWithUser(user);
    }

    private String genLiveIDWithUser(ZegoUser user) {
        // 使用房间ID 和 liveID 作为流的唯一标识，避免由于直接kill应用后，还会进行拉流，导致如果此时再次进入别的房间并以同一的标识推拉，之前的房间还可以听到流的声音。
        return MD5Utils.md5(getCurrentRoomID()) + "-" + user.userID;
    }

    public ZegoUser userForStreamID(String streamID) {
        if (streamIDForUser(mUserInfo).equals(streamID)) {
            return mUserInfo;
        }

        for (ZegoStreamInfo stream : mAllStreams) {
            if (stream.streamID.equals(streamID)) {
                ZegoUser user = new ZegoUser();
                user.userID = stream.userID;
                user.userName = stream.userName;

                return user;
            }
        }
        return null;
    }

    private ZegoStreamInfo streamForUser(@NonNull ZegoUser user) {
        for (ZegoStreamInfo stream : mAllStreams) {
            if (user.userID.equals(stream.userID)) {
                return stream;
            }
        }
        return null;
    }

    // ------------------- implements IZegoRoomCallback start ------------------- //
    @Override
    public void onKickOut(int reason, String roomID) {
        Log.d(TAG, String.format("onKickOut reason: %1$d, roomID: %2$s, currentRoomID: %3$s", reason, roomID, getCurrentRoomID()));

        if (!isCurrentRoom(roomID)) {
            return;
        }

        isDisconnect = true;

        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.KICK_OUT, ZegoRoomManagerErrorHelper.createResultCodeByKickOutReason(reason));

        callbackAllLiveStreamError();

        if (isAutoReconnectRoom) {
            endLoginRetryWithReason(ZegoRoomReconnectStopReason.KICK_OUT);
        }
    }

    @Override
    public void onDisconnect(int reason, String roomID) {
        Log.d(TAG, String.format("onDisconnect reason: %1$d, roomID: %2$s, currentRoomID: %3$s", reason, roomID, getCurrentRoomID()));

        if (!isCurrentRoom(roomID)) {
            return;
        }

        isDisconnect = true;

        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.DISCONNECT, ZegoRoomManagerErrorHelper.createResultCodeByRoomDisconnectReason(reason));

        callbackAllLiveStreamError();

        if (isAutoReconnectRoom) {
            mUIHandler.sendEmptyMessageDelayed(RECONNECT_ROOM_MESSAGE, mReconnectInterval);
        }
    }

    @Override
    public void onReconnect(int reason, String roomID) {
        if (!isCurrentRoom(roomID)) {
            return;
        }
        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.RECONNECT, ResultCode.RESULT_CODE_SUCCESS);
    }

    @Override
    public void onTempBroken(int reason, String roomID) {
        if (!isCurrentRoom(roomID)) {
            return;
        }
        updateLoginStatusWithLoginEvent(ZegoRoomLoginEvent.TEMP_BROKE, ZegoRoomManagerErrorHelper.createResultCodeByRoomTempBrokenReason(reason));

    }

    @Override
    public void onStreamUpdated(int updateType, ZegoStreamInfo[] streamList, String roomID) {
        if (!isCurrentRoom(roomID)) {
            return;
        }

        if (ZegoConstants.StreamUpdateType.Added == updateType) {
            addStreams(streamList);
        } else if (ZegoConstants.StreamUpdateType.Deleted == updateType) {
            deleteStreams(streamList);
        }
    }

    @Override
    public void onStreamExtraInfoUpdated(ZegoStreamInfo[] streamList, String roomID) {
        if (!isCurrentRoom(roomID)) {
            return;
        }
        for (ZegoStreamInfo stream : streamList) {
            ZegoUser user = new ZegoUser();
            user.userID = stream.userID;
            user.userName = stream.userName;
            setExtraInfo(stream.extraInfo, user);
        }
    }

    // ------------------- implements IZegoRoomCallback end ------------------- //


    // ------------------- implements IZegoLiveEventCallback start ------------------- //
    @Override
    public void onLiveEvent(int event, HashMap<String, String> hashMap) {
        String streamID = hashMap.get(LIVE_EVENT_STREAM_KEY);
        ZegoUser user = userForStreamID(streamID);

        int newLiveStatus;
        ResultCode resultCode = null;

        switch (event) {
            case ZegoLiveEventCallbackConstants.Play_TempDisconnected:
            case ZegoLiveEventCallbackConstants.Publish_TempDisconnected:
                resultCode = ZegoRoomManagerErrorHelper.createResultCodeByLiveEventTempBrokenReason();
                newLiveStatus = ZegoUserLiveStatus.WAIT_CONNECT;
                break;
            case ZegoLiveEventCallbackConstants.Play_BeginRetry:
            case ZegoLiveEventCallbackConstants.Publish_BeginRetry:
                newLiveStatus = ZegoUserLiveStatus.CONNECTING;
                break;
            case ZegoLiveEventCallbackConstants.Play_RetrySuccess:
            case ZegoLiveEventCallbackConstants.Publish_RetrySuccess:
                newLiveStatus = ZegoUserLiveStatus.LIVE;
                break;
            default:
                return;
        }

        setLiveStatus(newLiveStatus, resultCode, user);
    }

    @Override
    public void onRecvCustomCommand(String fromUserID, String fromUserName, String content, String roomID) {
        if (!isCurrentRoom(roomID)) {
            return;
        }

        ZegoUser fromUser = new ZegoUser();
        fromUser.userID = fromUserID;
        fromUser.userName = fromUserName;

        for (ZegoRoomManagerCallback callback : mManagerCallbacks) {
            callback.onRecvCustomCommand(fromUser, content);
        }
    }
    // ------------------- implements IZegoLiveEventCallback end ------------------- //


    // ------------------- implements IZegoLivePublisherCallback start ------------------- //
    @Override
    public void onPublishStateUpdate(int stateCode, String streamID, HashMap<String, Object> streamInfo) {
        Log.d(TAG, String.format("onPublishStateUpdate stateCode: %1$d, streamID: %2$s", stateCode, streamID));
        boolean success = stateCode == 0;

        if (success) {
            setStreamStatus(ZegoUserLiveStatus.LIVE, null, mUserInfo);
        } else {
            setStreamStatus(ZegoUserLiveStatus.WAIT_CONNECT, ZegoRoomManagerErrorHelper.createResultCodeByPublishState(stateCode), mUserInfo);

            updateCaptureFirstFrameIfNeed();

            if (isLogin()) {
                retryUserLiveWithUser(mUserInfo);
            }
        }
    }

    @Override
    public void onCaptureVideoSizeChangedTo(int width, int height) {
        ZegoUserLiveInfo liveInfo = liveInfoForUser(mUserInfo);
        if (liveInfo == null) {
            return;
        }

        if (!liveInfo.mFirstFrame) {
            setFirstFrame(true, width, height, mUserInfo);
        } else {
            // 分辨率改变回调
            for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                liveStatusCallback.onUserVideoFrameChange(mUserInfo, width, height);
            }
        }
    }

    @Override
    public void onPublishQualityUpdate(String streamID, ZegoPublishStreamQuality streamQuality) {
        ZegoUserLiveQuality quality = ZegoUserLiveQuality.initWithPublishQuality(streamQuality);
        setUserLiveQuality(quality, mUserInfo);
    }

    @Override
    public void onCaptureVideoFirstFrame() {
        // DO NOTHING
    }

    @Override
    public void onCaptureAudioFirstFrame() {

    }

    @Override
    public void onJoinLiveRequest(int seq, String fromUserID, String fromUserName, String roomID) {
        // DO NOTHING
    }

    @Override
    public void onMixStreamConfigUpdate(int stateCode, String mixStreamID, HashMap<String, Object> streamInfo) {
        // DO NOTHING
    }

    @Override
    public AuxData onAuxCallback(int dataLength) {
        // DO NOTHING
        return null;
    }
    // ------------------- implements IZegoLivePublisherCallback end ------------------- //

    // ------------------- implements IZegoLivePlayerCallback start ------------------- //

    @Override
    public void onPlayStateUpdate(int playState, String streamID) {
        boolean isSuccess = playState == 0;
        ZegoUser user = userForStreamID(streamID);
        if (user == null) {
            return;
        }

        if (isSuccess) {
            setStreamStatus(ZegoUserLiveStatus.LIVE, null, user);
        } else {
            setStreamStatus(ZegoUserLiveStatus.WAIT_CONNECT, ZegoRoomManagerErrorHelper.createResultCodeByPlayState(playState), user);

            // 重置firstFrame
            setFirstFrame(false, 0, 0, user);

            if (isLogin()) {
                retryUserLiveWithUser(user);
            }
        }
    }

    @Override
    public void onPlayQualityUpdate(String streamID, ZegoPlayStreamQuality streamQuality) {
        ZegoUser user = userForStreamID(streamID);
        setUserLiveQuality(ZegoUserLiveQuality.initWithPlayQuality(streamQuality), user);
    }

    @Override
    public void onVideoSizeChangedTo(String streamID, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        ZegoUser user = userForStreamID(streamID);
        if (user == null) {
            return;
        }

        ZegoUserLiveInfo info = mLiveInfos.get(user);
        if (info == null) {
            return;
        }

        if (!info.mFirstFrame) {
            setFirstFrame(true, width, height, user);

            // 设置播放视图和视图模式
            if (info.mVideoView != null) {
                mZegoLiveRoom.setViewMode(info.mViewMode, streamID);
                mZegoLiveRoom.updatePlayView(streamID, info.mVideoView);
            }
        } else {
            // 分辨率改变回调
            for (ZegoRoomManagerLiveStatusCallback liveStatusCallback : mLiveStatusCallbacks) {
                liveStatusCallback.onUserVideoFrameChange(user, width, height);
            }
        }
    }

    @Override
    public void onInviteJoinLiveRequest(int seq, String fromUserID, String fromUserName, String roomID) {
        // DO NOTHING
    }

    @Override
    public void onRecvEndJoinLiveCommand(String fromUserID, String fromUserName, String roomID) {
        // DO NOTHING
    }
    // ------------------- implements IZegoLivePlayerCallback end ------------------- //


    // ------------------- implements IZegoSoundLevelCallback start ------------------- //
    @Override
    public void onSoundLevelUpdate(ZegoSoundLevelInfo[] soundLevels) {
        for (ZegoSoundLevelInfo soundLevelInfo : soundLevels) {
            ZegoUser user = userForStreamID(soundLevelInfo.streamID);
            setSoundLevel(soundLevelInfo.soundLevel, user);
        }
    }

    @Override
    public void onCaptureSoundLevelUpdate(ZegoSoundLevelInfo captureSoundLevel) {
        setSoundLevel(captureSoundLevel.soundLevel, mUserInfo);
    }
    // ------------------- implements IZegoSoundLevelCallback end ------------------- //

    // ------------------- getter setter 方法  ------------------- //

    public ZegoRoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    public int getLoginStatus() {
        return mLoginStatus;
    }

    public boolean isLogin() {
        return mLoginStatus == ZegoRoomLoginStatus.LOGIN || mLoginStatus == ZegoRoomLoginStatus.TEMP_BROKE;
    }

    public Map<ZegoUser, ZegoUserLiveInfo> getLiveInfos() {
        return mLiveInfos;
    }

    public boolean isCurrentRoom(String roomID) {
        String currentRoomID = getCurrentRoomID();

        if (currentRoomID == null || !currentRoomID.equals(roomID)) {
            return false;
        }
        return true;
    }

    public String getCurrentRoomID() {
        return mRoomInfo == null ? null : mRoomInfo.mRoomID;
    }
}
