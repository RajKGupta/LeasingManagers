package com.example.rajk.leasingmanagers.model;

/**
 * Created by SoumyaAgarwal on 6/6/2017.
 */

public class measurement
{
    private String tag;
    private String width;
    private String height;
    private String fleximage;

    public measurement(String tag, String width, String height, String fleximage) {
        this.tag = tag;
        this.width = width;
        this.height = height;
        this.fleximage = fleximage;
    }

    public measurement() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getFleximage() {
        return fleximage;
    }

    public void setFleximage(String fleximage) {
        this.fleximage = fleximage;
    }
}
