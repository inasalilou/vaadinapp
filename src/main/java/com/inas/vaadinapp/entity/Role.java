package com.inas.vaadinapp.entity;

public enum Role {
    ADMIN("Administrateur"),
    ORGANIZER("Organisateur"),
    CLIENT("Client");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
