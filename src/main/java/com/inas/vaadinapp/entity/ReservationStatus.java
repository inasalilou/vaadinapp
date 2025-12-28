package com.inas.vaadinapp.entity;

public enum ReservationStatus {
    EN_ATTENTE("En attente", "contrast"),
    CONFIRMEE("Confirmée", "success"),
    ANNULEE("Annulée", "error");

    private final String label;
    private final String color;

    ReservationStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
