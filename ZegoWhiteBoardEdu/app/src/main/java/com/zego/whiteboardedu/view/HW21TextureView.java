package com.zego.whiteboardedu.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;

public class HW21TextureView extends TextureView {

    public HW21TextureView(Context context) {
        super(context);
    }

    public HW21TextureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HW21TextureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 强制设置成父布局提供的或者自身定义的宽度，并且计算高度，强制设置为EXACTLY
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = heightSize / 2;

        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
    }


}
