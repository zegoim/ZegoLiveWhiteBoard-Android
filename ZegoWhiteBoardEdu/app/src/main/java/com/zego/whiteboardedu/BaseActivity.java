package com.zego.whiteboardedu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.zego.whiteboardedu.utils.UiUtils;

/**
 * Activity 抽象基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    /**
     * 申请权限 code
     */
    protected static final int PERMISSIONS_REQUEST_CODE = 1002;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 申请权限
        checkOrRequestPermission(PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * 获取contentView
     *
     * @return 返回contentView
     */
    protected View getContentView() {
        ViewGroup contentLayout = getWindow().getDecorView().findViewById(android.R.id.content);
        return contentLayout != null && contentLayout.getChildCount() != 0 ? contentLayout.getChildAt(0) : null;
    }

    // 相机存储音频权限申请
    private static String[] PERMISSIONS_REQUEST = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

    /**
     * 检查并申请权限
     *
     * @param requestCode requestCode
     * @return 权限是否已经允许
     */
    protected boolean checkOrRequestPermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(PERMISSIONS_REQUEST, requestCode);
                return false;
            }
        }
        return true;
    }
}
