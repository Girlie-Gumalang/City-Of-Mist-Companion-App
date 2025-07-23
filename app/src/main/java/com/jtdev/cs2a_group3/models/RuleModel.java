package com.jtdev.cs2a_group3.models;

public class RuleModel {
    String title;
    String description;

    public RuleModel(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
