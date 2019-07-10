package com.zego.whiteboardedu.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 长宽1：1 TextureView，长宽都会默认修改为固定值，因此wrap_content可能会失效。
 */
public class HW11TextureView extends TextureView {

    public HW11TextureView(Context context) {
        super(context);
    }

    public HW11TextureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HW11TextureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 强制设置成父布局提供的或者自身定义的宽度，并且计算高度，强制设置为EXACTLY
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        super.onMeasure(MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
    }
}
