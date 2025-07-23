package com.jtdev.cs2a_group3.models;

import com.google.gson.annotations.SerializedName;

public class Improvement {
    @SerializedName("description")
    private String description;
    @SerializedName("checked")
    private boolean checked;

    public Improvement(String description, boolean checked) {
        this.description = description;
        this.checked = checked;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}