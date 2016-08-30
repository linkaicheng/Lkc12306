package com.cheng.lkc12306.bean;

import java.util.List;

/**
 * Created by cheng on 2016/8/30 0030.
 */
public class Order {
    private String id;
    private List<OrderPassenger> passengerList;
    private Train train;
    private int status;
    private String orderTime;
    private float orderPrice;

    public float getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(float orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<OrderPassenger> getPassengerList() {
        return passengerList;
    }

    public void setPassengerList(List<OrderPassenger> passengerList) {
        this.passengerList = passengerList;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", passengerList=" + passengerList +
                ", train=" + train +
                ", status=" + status +
                ", orderTime='" + orderTime + '\'' +
                ", orderPrice=" + orderPrice +
                '}';
    }
}
