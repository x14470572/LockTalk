package com.example.markenteder.locktalk;

public class Users {

    public String fullname, image, username;

    public Users(){

    }

    public Users(String fullname, String image, String username) {
        this.fullname = fullname;
        this.image = image;
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }




}
