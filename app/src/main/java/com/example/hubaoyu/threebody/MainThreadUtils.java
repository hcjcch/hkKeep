package com.example.hubaoyu.threebody;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;

import java.lang.reflect.Field;

/**
 * Utils for main thread
 *
 * @author chentian
 */
public class MainThreadUtils {

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static boolean isRelease;

    private MainThreadUtils() {
    }

    public static void init(boolean isRelease) {
        MainThreadUtils.isRelease = isRelease;
    }

    public static void post(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }

    /**
     * 延迟执行操作
     */
    public static void postDelayed(Runnable runnable, long delay) {
        mainThreadHandler.postDelayed(runnable, delay);
    }

    /**
     * 移除对应的Runnable
     */
    public static void removeRunnable(Runnable runnable) {
        mainThreadHandler.removeCallbacks(runnable);
    }

    /**
     * 检测是否在主线程上执行
     */
    public static void checkRunOnMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return;
        }

        if (!isRelease) {
            throw new AssertionError("Run this on non UI thread");
        }
    }

    /**
     * 在MessageQueue为空的时候执行
     */
    public static void runMessageQueueEmpty(MessageQueue.IdleHandler handler) {
        try {
            Field field = Looper.class.getDeclaredField("mQueue");
            field.setAccessible(true);
            MessageQueue queue = (MessageQueue) field.get(Looper.getMainLooper());
            queue.addIdleHandler(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测是否是主线程
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }
}
