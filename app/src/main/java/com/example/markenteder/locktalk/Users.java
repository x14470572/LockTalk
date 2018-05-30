package com.example.markenteder.locktalk;

public class Users {

    public String fullname, image, username, small_image;

    public Users(){

    }

    public Users(String fullname, String image, String username, String small_image) {
        this.fullname = fullname;
        this.image = image;
        this.username = username;
        this.small_image = small_image;
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

    public String getSmallImage() {
        return small_image;
    }

    public void setSmallImage(String small_image) {
        this.small_image = small_image;
    }
}
