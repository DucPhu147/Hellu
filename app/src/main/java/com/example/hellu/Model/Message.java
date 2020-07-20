package com.example.hellu.Model;

public class Message {
    private String sender;
    private String message;
    private String seen;
    private long timestamp;
    private String id;
    private String type;
    public Message(){

    }

    public Message(String id, String sender, String message, String seen, long timestamp,String type) {
        this.id=id;
        this.sender = sender;
        this.message = message;
        this.seen=seen;
        this.timestamp=timestamp;
        this.type=type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}
