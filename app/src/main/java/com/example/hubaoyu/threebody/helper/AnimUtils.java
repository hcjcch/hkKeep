package com.example.hubaoyu.threebody.helper;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;

/**
 * AnimUtils
 *
 * @author huangchen
 */
public class AnimUtils {
    public static void showPerfect(final TextView perfect) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(perfect, View.SCALE_X, 0.5f, 1f, 1.2f, 1f);
        objectAnimator.setDuration(1000);
        perfect.setVisibility(View.VISIBLE);
        objectAnimator.start();
        perfect.postDelayed(new Runnable() {
            @Override
            public void run() {
                perfect.setVisibility(View.GONE);
            }
        }, 1500);
    }
}