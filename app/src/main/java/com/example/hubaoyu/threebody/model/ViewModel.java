package com.example.hubaoyu.threebody.model;

/**
 * ViewModel
 *
 * @author huangchen
 */
public class ViewModel {
    private String statusString;
    private String toastString;
    private int count;

    public ViewModel(String statusString, String toastString, int count) {
        this.statusString = statusString;
        this.toastString = toastString;
        this.count = count;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public String getToastString() {
        return toastString;
    }

    public void setToastString(String toastString) {
        this.toastString = toastString;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public static ViewModel squatToViewModel(SquatModel source) {
        String statusString = "";
        if (source.getStatus() == 1) {
            statusString = "站立";
        } else if (source.getStatus() == 2) {
            statusString = "屈身";
        } else if (source.getStatus() == 3) {
            statusString = "蹲下";
        }
        String toastString = source.getToast();
        int count = source.getCount();
        return new ViewModel(statusString, toastString, count);
    }
}