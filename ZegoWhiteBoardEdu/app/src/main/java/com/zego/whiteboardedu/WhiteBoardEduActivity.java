package com.zego.whiteboardedu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UpdateCursor;
import com.zego.whiteboardedu.data.ZegoDataCenter;
import com.zego.whiteboardedu.manager.ZegoApiManager;
import com.zego.whiteboardedu.manager.entity.ResultCode;
import com.zego.whiteboardedu.manager.entity.ZegoUser;
import com.zego.whiteboardedu.manager.room.ZegoLoginRoomCallback;
import com.zego.whiteboardedu.manager.room.ZegoRoomInfo;
import com.zego.whiteboardedu.manager.room.ZegoRoomManager;
import com.zego.whiteboardedu.manager.room.ZegoRoomManagerCallback;
import com.zego.whiteboardedu.manager.room.ZegoRoomManagerLiveStatusCallback;
import com.zego.whiteboardedu.manager.room.ZegoUserLiveQuality;
import com.zego.whiteboardedu.utils.UiUtils;
import com.zego.zegoliveroom.constants.ZegoVideoViewMode;

import java.util.Random;

public class WhiteBoardEduActivity extends BaseActivity implements RoomCallbacks, ZegoRoomManagerCallback, ZegoRoomManagerLiveStatusCallback {

    private final static String TAG = WhiteBoardEduActivity.class.getSimpleName();

    public final static String EXTRA_ROOM_ID = "room_id";
    public final static String EXTRA_TEACHER_ID = "teacher_id";

    public final static String EXTRA_WHITE_UUID = "white_uuid";
    public final static String EXTRA_WHITE_ROOM_TOKEN = "white_room_token";

    private final static int MIN_THICKNESS = 1;
    private final static int MAX_THICKNESS = 10;

    private final static double RATIO_OF_THICKNESS_TO_TEXT_SIZE = 1.5;

    private String mRoomID;
    private String mTeacherID;

    private String mWhiteUUID;
    private String mWhiteRoomToken;

    private TextureView mTtvAnchor;
    private TextureView mTtvJoiner;
    private WhiteBroadView mWhiteBroadView;
    private TextView mTvCurrentThickness;

    private ScrollView mSvTools;

    private ZegoRoomManager mRoomManager;

    private WhiteSdk mWhiteSdk;
    private Room mWhiteRoom;

    // MemberState 相关值
    private int mCurrentThickness = 1;
    private int[] mColorArray = {0, 0, 0};

    private MemberState mCurrentMemberState;

    private Random mRandom = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_white_board_edu);

        initData();

        initView();

        initWhiteSdk();

        initRoomManager();

        joinRoom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置全屏
        UiUtils.setFullScreen(getWindow());
    }

    private void initData() {
        Intent intent = getIntent();
        mRoomID = intent.getStringExtra(EXTRA_ROOM_ID);
        mTeacherID = intent.getStringExtra(EXTRA_TEACHER_ID);

        mWhiteUUID = intent.getStringExtra(EXTRA_WHITE_UUID);
        mWhiteRoomToken = intent.getStringExtra(EXTRA_WHITE_ROOM_TOKEN);
    }

    private void initView() {
        mTtvAnchor = findViewById(R.id.ttv_anchor);
        mTtvJoiner = findViewById(R.id.ttv_joiner);
        mWhiteBroadView = findViewById(R.id.white_broad_view);

        mSvTools = findViewById(R.id.sv_tools);

        mTvCurrentThickness = findViewById(R.id.tv_current_thickness);
        mTvCurrentThickness.setText(String.valueOf(mCurrentThickness));
    }

    private void initWhiteSdk() {
        WhiteSdkConfiguration whiteSdkConfiguration = new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1);
        mWhiteSdk = new WhiteSdk(mWhiteBroadView, this, whiteSdkConfiguration);
    }

    private void initRoomManager() {
        mRoomManager = ZegoRoomManager.managerWithLiveRoom(ZegoApiManager.getInstance().getZegoLiveRoom(), false);
        // 无限尝试重连
        mRoomManager.setAutoReconnectRoom(true);
        mRoomManager.setReconnectTimeoutSec(0);
        // 设置回调监听
        mRoomManager.addManagerCallback(this);
        mRoomManager.addLiveStatusCallback(this);
    }

    /**
     * 先登录 White，后登录LiveRoom
     */
    private void joinRoom() {
        RoomParams roomParams = new RoomParams(mWhiteUUID, mWhiteRoomToken);
        mWhiteSdk.joinRoom(roomParams, this, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mWhiteRoom = room;

                // 显示工具栏
                mSvTools.setVisibility(View.VISIBLE);

                // 初始化教具
                initWhiteMemberState();

                // 开启预览
                startPreview();

                // 登录LiveRoom
                joinLiveRoom();
            }

            @Override
            public void catchEx(SDKError sdkError) {
                Log.e(TAG, "joinRoom catchEx sdkError: " + sdkError.getJsStack());
            }
        });
    }

    private void startPreview() {
        mRoomManager.setLiveVideoView(mTtvJoiner, ZegoVideoViewMode.ScaleAspectFill, null);
        mRoomManager.startPreview();
    }

    private void joinLiveRoom() {
        ZegoRoomInfo roomInfo = new ZegoRoomInfo(mRoomID, "", null);
        mRoomManager.joinRoom(roomInfo, ZegoDataCenter.ZEGO_USER, new ZegoLoginRoomCallback() {
            @Override
            public void onLoginRoom(ResultCode resultCode) {
                boolean isSuccess = resultCode.isSuccess();
                // 登录成功
                if (isSuccess) {
                    mRoomManager.startLive();
                }
            }
        });
    }

    private long mExitTime = 0;

    @Override
    public void onBackPressed() {
        boolean needFinish = System.currentTimeMillis() - mExitTime < 2000;
        if (needFinish) {
            leaveRoom();
            finish();
        } else {
            Toast.makeText(this, "再按一次退出房间", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
    }

    private void leaveRoom() {
        mRoomManager.stopLive();
        mRoomManager.leaveRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRoomManager.leaveRoom();
        mRoomManager.release();
    }

    // --------------- white 教具 相关 --------------- //

    /**
     * 初始化教具
     */
    private void initWhiteMemberState() {
        randomColor(null);
        // 默认选择铅笔
        pencil(null);
    }

    public void pencil(View view) {
        MemberState memberState = new MemberState();

        memberState.setCurrentApplianceName(Appliance.PENCIL);
        memberState.setStrokeColor(mColorArray);
        memberState.setStrokeWidth(mCurrentThickness);

        setMemberState(memberState);
    }

    public void selector(View view) {
        MemberState memberState = new MemberState();
        memberState.setCurrentApplianceName(Appliance.SELECTOR);

        setMemberState(memberState);
    }

    public void rectangle(View view) {
        MemberState memberState = new MemberState();

        memberState.setCurrentApplianceName(Appliance.RECTANGLE);
        memberState.setStrokeColor(mColorArray);
        memberState.setStrokeWidth(mCurrentThickness);

        setMemberState(memberState);
    }

    public void ellipse(View view) {
        MemberState memberState = new MemberState();

        memberState.setCurrentApplianceName(Appliance.ELLIPSE);
        memberState.setStrokeColor(mColorArray);
        memberState.setStrokeWidth(mCurrentThickness);

        setMemberState(memberState);
    }

    public void text(View view) {
        MemberState memberState = new MemberState();

        memberState.setCurrentApplianceName(Appliance.TEXT);
        memberState.setStrokeColor(mColorArray);
        memberState.setTextSize(mCurrentThickness * RATIO_OF_THICKNESS_TO_TEXT_SIZE);

        setMemberState(memberState);
    }

    public void eraser(View view) {
        MemberState memberState = new MemberState();

        memberState.setCurrentApplianceName(Appliance.ERASER);

        setMemberState(memberState);
    }

    public void randomColor(View view) {
        int red = mRandom.nextInt(256);
        int blue = mRandom.nextInt(256);
        int green = mRandom.nextInt(256);
        mColorArray[0] = red;
        mColorArray[1] = blue;
        mColorArray[2] = green;

        if (mCurrentMemberState != null) {
            MemberState memberState = mCurrentMemberState;
            memberState.setStrokeColor(mColorArray);

            setMemberState(memberState);
        }
    }

    private void setMemberState(MemberState memberState) {
        this.mCurrentMemberState = memberState;
        mWhiteRoom.setMemberState(memberState);
    }

    public void thicknessDecrease(View view) {
        mCurrentThickness--;
        if (mCurrentThickness < MIN_THICKNESS) {
            mCurrentThickness = MIN_THICKNESS;
        }
        mTvCurrentThickness.setText(String.valueOf(mCurrentThickness));
        updateThickness();
    }

    public void thicknessIncrease(View view) {
        mCurrentThickness++;
        if (mCurrentThickness > MAX_THICKNESS) {
            mCurrentThickness = MAX_THICKNESS;
        }
        mTvCurrentThickness.setText(String.valueOf(mCurrentThickness));
        updateThickness();
    }

    private void updateThickness() {
        if (mCurrentMemberState == null) {
            return;
        }
        MemberState memberState = mCurrentMemberState;
        String currentAppliance = memberState.getCurrentApplianceName();
        if (Appliance.TEXT.equals(currentAppliance)) {
            memberState.setTextSize(mCurrentThickness * RATIO_OF_THICKNESS_TO_TEXT_SIZE);
        } else {
            memberState.setStrokeWidth(mCurrentThickness);
        }
        setMemberState(memberState);
    }

    // ---------------- implements RoomCallbacks ---------------- //
    @Override
    public void onPhaseChanged(RoomPhase roomPhase) {
        Log.d(TAG, "onPhaseChanged roomPhase: " + roomPhase.name());
    }

    @Override
    public void onBeingAbleToCommitChange(boolean b) {

    }

    @Override
    public void onDisconnectWithError(Exception e) {

    }

    @Override
    public void onKickedWithReason(String s) {

    }

    @Override
    public void onRoomStateChanged(RoomState roomState) {

    }

    @Override
    public void onCatchErrorWhenAppendFrame(long l, Exception e) {

    }

    @Override
    public void onCursorViewsUpdate(UpdateCursor updateCursor) {

    }

    // ---------------- implements ZegoRoomManagerCallback ---------------- //
    @Override
    public void onLoginEventOccur(int event, int status, ResultCode errorCode) {

    }

    @Override
    public void onLiveUserJoin(ZegoUser user) {

    }

    @Override
    public void onLiveUserLeave(ZegoUser user) {

    }

    @Override
    public void onAutoReconnectStop(int stopReason) {

    }

    @Override
    public void onRecvCustomCommand(ZegoUser fromUser, String content) {

    }

    // ---------------- implements ZegoRoomManagerLiveStatusCallback ---------------- //
    @Override
    public void onLiveStatusChange(ZegoUser user, int liveStatus, ResultCode errorCode) {

    }

    @Override
    public void onUserGetFirstFrame(ZegoUser user, int width, int height) {
        if (mTeacherID.equals(user.userID)) {
            mRoomManager.setLiveVideoView(mTtvAnchor, ZegoVideoViewMode.ScaleAspectFill, user);
        }
    }

    @Override
    public void onUserVideoFrameChange(ZegoUser user, int width, int height) {

    }

    @Override
    public void onGetSoundLevel(ZegoUser user, float soundLevel) {

    }

    @Override
    public void onExtraInfoUpdate(ZegoUser user, String extraInfo) {

    }

    @Override
    public void onLiveQualityUpdate(ZegoUser user, ZegoUserLiveQuality quality) {

    }
}
