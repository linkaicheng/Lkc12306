package com.cheng.lkc12306.bean;

/**
 * Created by cheng on 2016/8/30 0030.
 */
public class OrderPassenger {
    private String id;
    private String name;
    private String idType;
    private String tel;
    private String type;
    private Seat seat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    @Override
    public String toString() {
        return "OrderPassenger{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", idType='" + idType + '\'' +
                ", tel='" + tel + '\'' +
                ", type='" + type + '\'' +
                ", seat=" + seat +
                '}';
    }
}
