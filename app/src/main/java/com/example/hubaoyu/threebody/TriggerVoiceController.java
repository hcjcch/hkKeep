package com.example.hubaoyu.threebody;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * 触发式语音
 *
 * @author huangchen
 */
public class TriggerVoiceController {
    private String audioPath = "";
    private Context context;
    private MediaPlayer mediaPlayer;

    public TriggerVoiceController(Context context) {
        this.context = context;
        mediaPlayer = new MediaPlayer();
    }

    public void play(String audioPath) {
        if (this.audioPath.equals(audioPath)) {
            return;
        }
        try {
            this.audioPath = audioPath;
            play();
        } catch (IOException ignore) {
        }
    }

    public void resume() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void play() throws IOException {
        mediaPlayer.setVolume(1.0f, 1.0f);
        setMediaData(audioPath);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    private void setMediaData(String audioPath) throws IOException {
        mediaPlayer.reset();
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor fileDescriptor = assetManager.openFd(audioPath);
        mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                fileDescriptor.getLength());
    }
}