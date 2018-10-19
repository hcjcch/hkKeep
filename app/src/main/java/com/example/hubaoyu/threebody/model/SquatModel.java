package com.example.hubaoyu.threebody.model;

import java.util.List;

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
    private List<Double> angles;

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

    public List<Double> getAngles() {
        return angles;
    }

    @Override
    public String toString() {
        return "SquatModel{" +
                "status=" + status +
                ", toast='" + toast + '\'' +
                ", count=" + count +
                ", warning='" + warning + '\'' +
                ", angles=" + angles +
                '}';
    }
}