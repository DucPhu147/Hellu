package com.example.hellu.MessageNotification;

public class Data {
    private String sender;
    private String body;
    private String title;
    private String largeicon;
    private String receiver;
    public Data() {
    }

    public Data(String sender, String body, String title, String receiver,String largeIcon) {
        this.sender = sender;
        this.body = body;
        this.title = title;
        this.receiver = receiver;
        this.largeicon=largeIcon;
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

    public String getLargeicon() {
        return largeicon;
    }

    public void setLargeicon(String largeicon) {
        this.largeicon = largeicon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
