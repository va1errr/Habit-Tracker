package com.va1err.habittracker.entity;

public class Habit {

    private String name;

    private String description;

    private boolean active;

    public Habit(String name, String description, boolean active) {
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

}
