package com.zego.whiteboardedu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.whiteboardedu.adapter.RoomListAdapter;
import com.zego.whiteboardedu.entity.RoomInfo;
import com.zego.whiteboardedu.helper.RoomListHelper;

import java.util.List;

public class RoomListActivity extends BaseActivity implements RoomListHelper.RoomListUpdateListener, RoomListAdapter.OnRoomInfoItemClickListener, View.OnClickListener {

    private LinearLayout mLlStatus;
    private TextView mTvStatusTitle;
    private TextView mTvStatusTip;
    private RecyclerView mRvRoomList;

    private RoomListAdapter mRoomListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        initView();

        RoomListHelper.shared().setRoomListUpdateListener(this);
        RoomListHelper.shared().fetchRoomList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        RoomListHelper.shared().fetchRoomList();
    }

    private void initView() {
        mLlStatus = findViewById(R.id.ll_status);
        mTvStatusTitle = findViewById(R.id.tv_status_title);
        mTvStatusTip = findViewById(R.id.tv_status_tip);
        mRvRoomList = findViewById(R.id.rv_room_list);

        findViewById(R.id.tv_refresh).setOnClickListener(this);
        findViewById(R.id.tv_no_network_refresh).setOnClickListener(this);

        initRoomListRecyclerView();
    }

    private void initRoomListRecyclerView() {
        mRvRoomList.setLayoutManager(new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false));
        mRoomListAdapter = new RoomListAdapter();
        mRoomListAdapter.setOnRoomInfoItemClickListener(this);

        mRvRoomList.setAdapter(mRoomListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_refresh:
            case R.id.tv_no_network_refresh:
                RoomListHelper.shared().fetchRoomList();
                break;
        }
    }

    @Override
    public void onRoomListUpdate(List<RoomInfo> roomInfoList) {
        if (roomInfoList == null) {
            // 如果为null，则说明请求的时候发生错误
            showNetworkError();
        } else if (roomInfoList.isEmpty()) {
            // 请求正常返回，但是没有房间
            showNoRoom();
        } else {
            showRoomList();
        }

        mRoomListAdapter.setRoomInfoList(roomInfoList);
    }

    @Override
    public void onRoomInfoItemClick(RoomInfo roomInfo) {
        boolean needReplay = TextUtils.isEmpty(roomInfo.teacherId);
        if (needReplay) {
            startReplayActivity(roomInfo);
        } else {
            startWhiteBoardEduActivity(roomInfo);
        }
    }

    private void startReplayActivity(RoomInfo roomInfo) {
        Intent intent = new Intent(this, ReplayActivity.class);
        intent.putExtra(ReplayActivity.EXTRA_REPLAY_URL, roomInfo.replayUrl);
        intent.putExtra(ReplayActivity.EXTRA_CREATE_TIMESTAMP, roomInfo.createAt);

        intent.putExtra(ReplayActivity.EXTRA_WHITE_UUID, roomInfo.whiteScreen.uuid);
        startActivity(intent);
    }

    private void startWhiteBoardEduActivity(RoomInfo roomInfo) {
        if (!checkOrRequestPermission(PERMISSIONS_REQUEST_CODE)) {
            Toast.makeText(this, "请到设置页面允许摄像头和麦克风权限！", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, WhiteBoardEduActivity.class);
            intent.putExtra(WhiteBoardEduActivity.EXTRA_ROOM_ID, roomInfo.roomId);
            intent.putExtra(WhiteBoardEduActivity.EXTRA_TEACHER_ID, roomInfo.teacherId);
            intent.putExtra(WhiteBoardEduActivity.EXTRA_WHITE_UUID, roomInfo.whiteScreen.uuid);
            intent.putExtra(WhiteBoardEduActivity.EXTRA_WHITE_ROOM_TOKEN, roomInfo.whiteScreen.roomToken);
            startActivity(intent);
        }
    }

    private void showNetworkError() {
        mLlStatus.setVisibility(View.VISIBLE);
        mTvStatusTitle.setText(R.string.no_network);
        mTvStatusTip.setText(R.string.no_network_tip);
        mRvRoomList.setVisibility(View.GONE);
    }

    private void showNoRoom() {
        mLlStatus.setVisibility(View.VISIBLE);
        mTvStatusTitle.setText(R.string.no_room);
        mTvStatusTip.setText(R.string.no_room_tip);
        mRvRoomList.setVisibility(View.GONE);
    }

    private void showRoomList() {
        mLlStatus.setVisibility(View.GONE);
        mRvRoomList.setVisibility(View.VISIBLE);
    }
}
