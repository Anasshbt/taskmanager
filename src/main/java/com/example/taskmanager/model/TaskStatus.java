package com.example.taskmanager.model;

public enum TaskStatus {
    TODO("À faire"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}