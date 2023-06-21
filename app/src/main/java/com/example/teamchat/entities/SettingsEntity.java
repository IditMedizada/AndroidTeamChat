package com.example.teamchat.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SettingsEntity {

    @PrimaryKey(autoGenerate = true)
    int id;

    private String url;
    private boolean isNightMode;

    public SettingsEntity(String url, boolean isNightMode) {
        this.url = url;
        this.isNightMode = isNightMode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getNightMode() {
        return isNightMode;
    }

    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}