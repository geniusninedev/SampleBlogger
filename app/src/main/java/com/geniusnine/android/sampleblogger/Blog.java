package com.geniusnine.android.sampleblogger;

/**
 * Created by Dev on 10-02-2017.
 */

public  class Blog {
    public Blog(String title, String desc, String image) {
        this.title = title;
        this.desc = desc;
        this.image = image;
    }
    public Blog(){}

    private String title;
    private String desc;
    private String image;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }



}
