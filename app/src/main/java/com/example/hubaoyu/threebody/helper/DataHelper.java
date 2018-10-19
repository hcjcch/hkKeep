package com.example.hubaoyu.threebody.helper;

/**
 * DataHelper
 *
 * @author huangchen
 */
public class DataHelper {
    public static final int LEVEL_THIRD = 2;
    public static final int LEVEL_SECOND = 1;
    public static final int LEVEL_FIRST = 0;

    public String countToAudio(int count) {
        if (count < 10) {
            return "N00" + count + ".mp3";
        } else {
            return "N0" + count + ".mp3";
        }
    }

    public int levelCheck(double angle) {
        if (angle > 0.4) {
            return LEVEL_THIRD;
        } else if (angle >= 0.2 && angle <= 0.4) {
            return LEVEL_SECOND;
        } else {
            return LEVEL_FIRST;
        }
    }
}