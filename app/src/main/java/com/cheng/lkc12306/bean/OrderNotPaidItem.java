package com.cheng.lkc12306.bean;

import java.io.Serializable;

/**
 * Created by cheng on 2016/9/1 0001.
 */
public class OrderNotPaidItem implements Serializable {
    private String name;
    private String trainNo;
    private String date;
    private String seat;

    @Override
    public String toString() {
        return "OrderNotPaidItem{" +
                "name='" + name + '\'' +
                ", trainNo='" + trainNo + '\'' +
                ", date='" + date + '\'' +
                ", seat='" + seat + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }
}
