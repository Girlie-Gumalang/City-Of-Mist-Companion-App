package com.jtdev.cs2a_group3.models;

public class Crewmate {
    private String name;
    private int helpValue;
    private int hurtValue;

    public Crewmate(String name, int helpValue, int hurtValue) {
        this.name = name;
        this.helpValue = helpValue;
        this.hurtValue = hurtValue;
    }


    public Crewmate() {
        this.name = "";
        this.helpValue = 0;
        this.hurtValue = 0;
    }


    public String getName() { return name; }
    public int getHelpValue() { return helpValue; }
    public int getHurtValue() { return hurtValue; }


    public void setName(String name) { this.name = name; }
    public void setHelpValue(int helpValue) { this.helpValue = helpValue; }
    public void setHurtValue(int hurtValue) { this.hurtValue = hurtValue; }
}