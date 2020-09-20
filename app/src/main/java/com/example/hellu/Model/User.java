package com.example.hellu.Model;

import android.os.Parcelable;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String username;
    private String imageURL;
    private String status;
    private long lastonline;
    private String email;
    private String search;

    public User(){

    }
    public User(String id, String username, String email, String imgURL, String status,long lastSeen,String search)
    {
        this.id=id;
        this.username=username;
        this.email=email;
        this.imageURL=imgURL;
        this.status=status;
        this.lastonline =lastSeen;
        this.search=search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public long getLastonline() {
        return lastonline;
    }

    public void setLastonline(long lastonline) {
        this.lastonline = lastonline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
