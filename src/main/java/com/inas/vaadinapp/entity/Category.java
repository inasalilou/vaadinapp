package com.inas.vaadinapp.entity;

public enum Category {
    CONCERT("Concert"),
    SPORT("Sport"),
    CONFERENCE("Conférence"),
    FESTIVAL("Festival"),
    AUTRE("Autre");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
