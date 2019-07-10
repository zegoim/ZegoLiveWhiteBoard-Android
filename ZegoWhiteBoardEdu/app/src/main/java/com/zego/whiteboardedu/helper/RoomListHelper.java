package com.zego.whiteboardedu.helper;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.zego.whiteboardedu.entity.RoomInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomListHelper {

    private final static String TAG = RoomListHelper.class.getSimpleName();

    private final static class RoomListHelperHolder {
        private final static RoomListHelper sInstance = new RoomListHelper();
    }

    public static RoomListHelper shared() {
        return RoomListHelperHolder.sInstance;
    }

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    private OkHttpClient mClient;

    private Gson mGson;

    private RoomListUpdateListener mRoomListUpdateListener;

    private boolean isFetching;

    private RoomListHelper() {
        mClient = new OkHttpClient();
        mGson = new Gson();
        isFetching = false;
    }

    public void setRoomListUpdateListener(RoomListUpdateListener roomListUpdateListener) {
        mRoomListUpdateListener = roomListUpdateListener;
    }

    public void fetchRoomList() {
        if (isFetching) {
            return;
        }
        fetchRoomListInner();
    }

    private void fetchRoomListInner() {
        final Request request = new Request.Builder()
                .url("http://118.25.189.55:8092/class-room/rooms")
                .get()
                .build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isFetching = false;
                Log.d(TAG, "onFailure e: " + e.getMessage());
                if (mRoomListUpdateListener != null) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mRoomListUpdateListener.onRoomListUpdate(null);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isFetching = false;
                if (response.isSuccessful()) {
                    JsonArray roomJsonArray = mGson.fromJson(response.body().string(), JsonArray.class);

                    final List<RoomInfo> roomInfoList = new ArrayList<>(roomJsonArray.size());

                    for (int i = 0; i < roomJsonArray.size(); i++) {
                        RoomInfo roomInfo = mGson.fromJson(roomJsonArray.get(i), RoomInfo.class);
                        roomInfoList.add(roomInfo);
                        Log.d(TAG, "-->:: " + roomInfo);
                    }

                    if (mRoomListUpdateListener != null) {
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mRoomListUpdateListener.onRoomListUpdate(roomInfoList);
                            }
                        });
                    }
                }
            }
        });
    }

    public interface RoomListUpdateListener {
        void onRoomListUpdate(List<RoomInfo> roomInfoList);
    }
}
