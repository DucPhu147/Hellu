package com.example.hellu.Model;

import java.io.Serializable;

public class Group implements Serializable {
    private String owner;
    private String name;
    private String imageURL;
    private String id;
    private String member;
    private String search;
    public Group(){

    }
    public Group(String id,String owner, String name, String imageURL,String member,String search) {
        this.id=id;
        this.owner = owner;
        this.name = name;
        this.imageURL = imageURL;
        this.member = member;
        this.search=search;
    }
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String memberid) {
        this.member = memberid;
    }
}
