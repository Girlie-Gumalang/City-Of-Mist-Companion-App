package com.jtdev.cs2a_group3.models;

public class StoryTag {
    private String tag;
    private boolean invokeChecked;

    public StoryTag(String tag, boolean invokeChecked) {
        this.tag = tag;
        this.invokeChecked = invokeChecked;
    }


    public StoryTag() {
        this.tag = "";
        this.invokeChecked = false;
    }
    public String getTag() { return tag; }
    public boolean isInvokeChecked() { return invokeChecked; }


    public void setTag(String tag) { this.tag = tag; }
    public void setInvokeChecked(boolean invokeChecked) { this.invokeChecked = invokeChecked; }
}
