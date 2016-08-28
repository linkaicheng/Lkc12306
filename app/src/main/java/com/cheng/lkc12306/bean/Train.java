package com.cheng.lkc12306.bean;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by cheng on 2016/8/26 0026.
 */
public class Train implements Serializable {
    private String trainNo;
    private String startStationName;
    private String endStationName;
    private String fromStationName;
    private String toStationName;
    private String startTime;
    private String arriveTime;
    private int dayDifference;
    private String durationTime;
    private String startTrainDate;
    private Map<String,Seat> seats;

    @Override
    public String toString() {
        return "Train{" +
                "trainNo='" + trainNo + '\'' +
                ", startStationName='" + startStationName + '\'' +
                ", endStationName='" + endStationName + '\'' +
                ", fromStationName='" + fromStationName + '\'' +
                ", toStationName='" + toStationName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", arriveTime='" + arriveTime + '\'' +
                ", dayDifference=" + dayDifference +
                ", durationTime='" + durationTime + '\'' +
                ", startTrainDate='" + startTrainDate + '\'' +
                ", seats=" + seats +
                '}';
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getStartStationName() {
        return startStationName;
    }

    public void setStartStationName(String startStationName) {
        this.startStationName = startStationName;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public void setEndStationName(String endStationName) {
        this.endStationName = endStationName;
    }

    public String getFromStationName() {
        return fromStationName;
    }

    public void setFromStationName(String fromStationName) {
        this.fromStationName = fromStationName;
    }

    public String getToStationName() {
        return toStationName;
    }

    public void setToStationName(String toStationName) {
        this.toStationName = toStationName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public int getDayDifference() {
        return dayDifference;
    }

    public void setDayDifference(int dayDifference) {
        this.dayDifference = dayDifference;
    }

    public String getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(String durationTime) {
        this.durationTime = durationTime;
    }

    public String getStartTrainDate() {
        return startTrainDate;
    }

    public void setStartTrainDate(String startTrainDate) {
        this.startTrainDate = startTrainDate;
    }

    public Map<String, Seat> getSeats() {
        return seats;
    }

    public void setSeats(Map<String, Seat> seats) {
        this.seats = seats;
    }
}
