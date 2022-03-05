package com.junling.mq.core.message;

import java.util.Date;

public class MqMessage {

    private Long id;
    private String topic;
    private String group;
    private String data;
    private String status;
    private Date updateTime;

    public void setId(Long id) {
        this.id = id;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroup() {
        return group;
    }

    public String getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }
}
