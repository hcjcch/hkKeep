package com.example.hubaoyu.threebody.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * 自定义字体的 TextView，使用 Keep 自定义的字体
 *
 * @author zhumengyang
 */
public class KeepFontTextView extends AppCompatTextView {

    private static final String FONT_FILE_PATH = "font/Keep.ttf";
    private static Typeface typeface;

    public KeepFontTextView(Context context) {
        super(context);
        applyTypeface(context);
    }

    public KeepFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeface(context);
    }

    public KeepFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyTypeface(context);
    }

    private void applyTypeface(Context context) {
        try {
            if (typeface == null) {
                typeface = Typeface.createFromAsset(context.getAssets(), FONT_FILE_PATH);
            }
            setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            setTypeface(typeface);
        } catch (Throwable ignore) {
        }
    }
}
