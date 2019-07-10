package com.zego.whiteboardedu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.herewhite.sdk.Player;
import com.herewhite.sdk.PlayerEventListener;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UpdateCursor;
import com.zego.whiteboardedu.utils.UiUtils;
import com.zego.zegoavkit2.IZegoMediaPlayerCallback;
import com.zego.zegoavkit2.ZegoMediaPlayer;

public class ReplayActivity extends Activity implements PlayerEventListener, IZegoMediaPlayerCallback {

    public final static String TAG = ReplayActivity.class.getSimpleName();

    public final static String EXTRA_REPLAY_URL = "replay_url";
    public final static String EXTRA_CREATE_TIMESTAMP = "create_timestamp";

    public final static String EXTRA_WHITE_UUID = "white_uuid";

    private String mReplayUrl;

    private String mWhiteUUID;

    private TextureView mTtvReplay;
    private ZegoMediaPlayer mMediaPlayer;

    private WhiteBroadView mReplayWhiteBroadView;

    private WhiteSdk mWhiteSdk;

    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        initData();

        initView();

        initWhiteSdk();

        initMediaPlayer();

        initWhitePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.setFullScreen(getWindow());
    }

    private void initData() {
        Intent intent = getIntent();
        mReplayUrl = intent.getStringExtra(EXTRA_REPLAY_URL);

        mWhiteUUID = intent.getStringExtra(EXTRA_WHITE_UUID);
    }

    private void initView() {
        mTtvReplay = findViewById(R.id.ttv_replay);
        mReplayWhiteBroadView = findViewById(R.id.white_broad_replay_view);
    }

    private void initWhiteSdk() {
        WhiteSdkConfiguration whiteSdkConfiguration = new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1);
        mWhiteSdk = new WhiteSdk(mReplayWhiteBroadView, this, whiteSdkConfiguration);
    }

    private void initMediaPlayer() {
        mMediaPlayer = new ZegoMediaPlayer();
        mMediaPlayer.init(ZegoMediaPlayer.PlayerTypePlayer);
        mMediaPlayer.setView(mTtvReplay);
        mMediaPlayer.load(mReplayUrl);
        mMediaPlayer.setCallback(this);
    }

    private void initWhitePlayer() {
        PlayerConfiguration playerConfiguration = new PlayerConfiguration();
        playerConfiguration.setRoom(mWhiteUUID);

        mWhiteSdk.createPlayer(playerConfiguration, this, new Promise<Player>() {
            @Override
            public void then(Player player) {
                mPlayer = player;
                player.play();
                // 获取白板开始录制的时间点
                player.getPlayerTimeInfo(new Promise<PlayerTimeInfo>() {
                    @Override
                    public void then(PlayerTimeInfo playerTimeInfo) {
                        startReplay();
                    }

                    @Override
                    public void catchEx(SDKError sdkError) {
                        Log.e(TAG, "sdkError: " + sdkError.getJsStack());
                    }
                });
            }

            @Override
            public void catchEx(SDKError sdkError) {
                Log.e(TAG, "sdkError: " + sdkError.getJsStack());
            }
        });
    }

    private void startReplay() {
        mMediaPlayer.resume();
        mPlayer.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mPlayer.stop();
    }

    // ----------------- implements PlayerEventListener ----------------- //
    @Override
    public void onPhaseChanged(PlayerPhase playerPhase) {
        Log.d(TAG, "onPhaseChanged playerPhase: " + playerPhase.name());
    }

    @Override
    public void onLoadFirstFrame() {

    }

    @Override
    public void onSliceChanged(String s) {

    }

    @Override
    public void onPlayerStateChanged(PlayerState playerState) {

    }

    @Override
    public void onStoppedWithError(SDKError sdkError) {

    }

    @Override
    public void onScheduleTimeChanged(long l) {

    }

    @Override
    public void onCatchErrorWhenAppendFrame(SDKError sdkError) {

    }

    @Override
    public void onCatchErrorWhenRender(SDKError sdkError) {

    }

    @Override
    public void onCursorViewsUpdate(UpdateCursor updateCursor) {

    }

    // ------------------ implements IZegoMediaPlayerCallback ------------------ //

    @Override
    public void onPlayStart() {
    }

    @Override
    public void onPlayPause() {
    }

    @Override
    public void onPlayStop() {
    }

    @Override
    public void onPlayResume() {
        Log.d(TAG, "-->:: onPlayResume");
    }

    @Override
    public void onPlayError(int i) {
        Log.d(TAG, "-->:: onPlayError error: " + i);
    }

    @Override
    public void onVideoBegin() {
    }

    @Override
    public void onAudioBegin() {
    }

    @Override
    public void onPlayEnd() {
    }

    @Override
    public void onBufferBegin() {
    }

    @Override
    public void onBufferEnd() {
    }

    @Override
    public void onSeekComplete(int i, long l) {
    }

    @Override
    public void onSnapshot(Bitmap bitmap) {
    }

    @Override
    public void onLoadComplete() {
    }

    @Override
    public void onProcessInterval(long l) {

    }
}
