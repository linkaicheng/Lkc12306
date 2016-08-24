package com.cheng.lkc12306.bean;

/**
 * Created by cheng on 2016/8/24.
 */
public class Passenger {

    /**
     * id : 37010519880414805X
     * name : 陈伟飞
     * idType : 身份证
     * tel : 13912341200
     * type : 成人
     */

    private String id;
    private String name;
    private String idType;
    private String tel;
    private String type;

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
}
