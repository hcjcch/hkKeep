package com.example.hubaoyu.threebody.helper;

import android.animation.AnimatorSet;
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
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(perfect, View.SCALE_X, 0.5f, 1f, 1.2f, 1f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(perfect, View.SCALE_Y, 0.5f, 1f, 1.2f, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1, objectAnimator2);
        animatorSet.setDuration(1000);
        animatorSet.start();
        perfect.setVisibility(View.VISIBLE);
        perfect.postDelayed(new Runnable() {
            @Override
            public void run() {
                perfect.setVisibility(View.GONE);
            }
        }, 1500);
    }
}