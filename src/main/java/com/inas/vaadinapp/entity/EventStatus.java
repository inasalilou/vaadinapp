package com.inas.vaadinapp.entity;

public enum EventStatus {
    BROUILLON("Brouillon", "contrast"),
    PUBLIE("Publié", "success"),
    ANNULE("Annulé", "error"),
    TERMINE("Terminé", "primary");

    private final String label;
    private final String color;

    EventStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Couleur "logique" (non liée à l'UI tant qu'elle n'est pas utilisée).
     */
    public String getColor() {
        return color;
    }
}
