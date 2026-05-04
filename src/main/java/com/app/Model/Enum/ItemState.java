package com.app.Model.Enum;

public enum ItemState {
    Nuevo("Nuevo"),
    Bueno("Bueno"),
    Regular("Regular"),
    Dañado("Dañado");

    private final String displayName;

    ItemState(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }
}
