package com.example.hubaoyu.threebody.helper;

/**
 * DataHelper
 *
 * @author huangchen
 */
public class DataHelper {
    public String countToAudio(int count) {
        if (count < 10) {
            return "N00" + count + ".mp3";
        } else {
            return "N0" + count + ".mp3";
        }
    }
}