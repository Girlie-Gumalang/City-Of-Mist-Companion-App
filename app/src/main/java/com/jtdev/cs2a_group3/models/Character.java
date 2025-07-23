package com.jtdev.cs2a_group3.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class Character implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("logos")
    private String logos;

    @SerializedName("mythos")
    private String mythos;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("buildUpStates")
    private List<Boolean> buildupStates;

    @SerializedName("improvementStates")
    private List<Boolean> improvementStates;

    @SerializedName("crewmates")
    private List<Crewmate> crewmates;

    @SerializedName("storyTags")
    private List<StoryTag> storyTags;

    @SerializedName("nemeses")
    private String nemeses;

    @SerializedName("notes")
    private String notes;

    @SerializedName("userId")
    private String userId;


    public Character() {
        this.id = -1;
        this.name = "New Character";
        this.logos = "";
        this.mythos = "";
        this.imageUrl = "";
        this.buildupStates = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            this.buildupStates.add(false);
        }
        this.improvementStates = new ArrayList<>();
        for (int i = 0; i < 11; i++) { // Assuming 11 improvements
            this.improvementStates.add(false);
        }
        this.crewmates = new ArrayList<>();
        this.storyTags = new ArrayList<>();
        this.nemeses = "";
        this.notes = "";
        this.userId = null;
    }

    public Character(String name, String userId) {
        this();
        this.name = name;
        this.userId = userId;
    }


    public Character(long id, String name, String logos, String mythos, String imageUrl,
                     List<Boolean> buildupStates, List<Boolean> improvementStates,
                     List<Crewmate> crewmates, List<StoryTag> storyTags,
                     String nemeses, String notes, String userId) {
        this.id = id;
        this.name = name;
        this.logos = logos;
        this.mythos = mythos;
        this.imageUrl = imageUrl;
        this.buildupStates = (buildupStates != null) ? new ArrayList<>(buildupStates) : new ArrayList<>();
        this.improvementStates = (improvementStates != null) ? new ArrayList<>(improvementStates) : new ArrayList<>();
        this.crewmates = (crewmates != null) ? new ArrayList<>(crewmates) : new ArrayList<>();
        this.storyTags = (storyTags != null) ? new ArrayList<>(storyTags) : new ArrayList<>();
        this.nemeses = nemeses;
        this.notes = notes;
        this.userId = userId;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getLogos() { return logos; }
    public String getMythos() { return mythos; }
    public String getImageUrl() { return imageUrl; }
    public List<Boolean> getBuildupStates() { return buildupStates; }
    public List<Boolean> getImprovementStates() { return improvementStates; }
    public List<Crewmate> getCrewmates() { return crewmates; }
    public List<StoryTag> getStoryTags() { return storyTags; }
    public String getNemeses() { return nemeses; }
    public String getNotes() { return notes; }
    public String getUserId() { return userId; }


    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogos(String logos) { this.logos = logos; }
    public void setMythos(String mythos) { this.mythos = mythos; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBuildupStates(List<Boolean> buildupStates) { this.buildupStates = (buildupStates != null) ? new ArrayList<>(buildupStates) : new ArrayList<>(); }
    public void setImprovementStates(List<Boolean> improvementStates) { this.improvementStates = (improvementStates != null) ? new ArrayList<>(improvementStates) : new ArrayList<>(); }
    public void setCrewmates(List<Crewmate> crewmates) { this.crewmates = (crewmates != null) ? new ArrayList<>(crewmates) : new ArrayList<>(); }
    public void setStoryTags(List<StoryTag> storyTags) { this.storyTags = (storyTags != null) ? new ArrayList<>(storyTags) : new ArrayList<>(); }
    public void setNemeses(String nemeses) { this.nemeses = nemeses; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setUserId(String userId) { this.userId = userId; }



    public String toDesiredJsonString() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }

    public static Character fromJsonString(String jsonString) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, Character.class);
    }
}