package com.example.hellu.Model;

import java.io.Serializable;

public class Calling implements Serializable {
    private String sender;
    private String receiver;
    private String imageURL;
    private String name;
    private String status;

    public Calling() {
    }

    public Calling(String sender, String receiver, String imageURL, String name,String status) {
        this.sender = sender;
        this.receiver = receiver;
        this.imageURL = imageURL;
        this.name = name;
        this.status=status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
