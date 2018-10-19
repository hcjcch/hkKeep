package com.example.hubaoyu.threebody.model;

/**
 * SquatModelResponse
 *
 * @author huangchen
 */
public class SquatModel {
    private int status;
    private String toast;
    private int count;
    private String warning;

    public int getStatus() {
        return status;
    }

    public String getToast() {
        return toast;
    }

    public int getCount() {
        return count;
    }

    public String getWarning() {
        return warning;
    }

    @Override
    public String toString() {
        return "SquatModel{" +
                "status=" + status +
                ", toast='" + toast + '\'' +
                ", count=" + count +
                ", warning='" + warning + '\'' +
                '}';
    }
}