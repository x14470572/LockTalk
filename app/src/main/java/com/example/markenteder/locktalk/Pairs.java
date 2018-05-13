package com.example.markenteder.locktalk;

public class Pairs {

    public String username, fullname;

    public Pairs(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Pairs(String username, String fullname) {
        this.username = username;
        this.fullname = fullname;
    }
}
