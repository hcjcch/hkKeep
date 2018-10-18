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

    public int getStatus() {
        return status;
    }

    public String getToast() {
        return toast;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "SquatModel{" +
                "status=" + status +
                ", toast='" + toast + '\'' +
                ", count=" + count +
                '}';
    }
}