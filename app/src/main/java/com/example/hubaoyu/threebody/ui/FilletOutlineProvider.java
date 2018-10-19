package com.example.hubaoyu.threebody.ui;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * 圆角 OutlineProvider
 *
 * @author huangchen
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FilletOutlineProvider extends ViewOutlineProvider {
    private float mRadius;

    public FilletOutlineProvider(float mRadius) {
        this.mRadius = mRadius;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        int leftMargin = 0;
        int topMargin = 0;
        Rect selfRect =
            new Rect(leftMargin, topMargin, rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
        outline.setRoundRect(selfRect, mRadius);
    }
}
