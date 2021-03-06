package com.cheng.lkc12306.bean;

import java.io.Serializable;

/**
 * Created by cheng on 2016/8/26 0026.
 */
public class Seat implements Serializable{
    private String seatName;
    private int seatNum;
    private float seatPrice;
    private String seatNo;

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public float getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(float seatPrice) {
        this.seatPrice = seatPrice;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatName='" + seatName + '\'' +
                ", seatNum=" + seatNum +
                ", seatPrice=" + seatPrice +
                ", seatNo='" + seatNo + '\'' +
                '}';
    }
}
