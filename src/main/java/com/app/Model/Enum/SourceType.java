package com.app.Model.Enum;

public enum SourceType {
    EMPENO("Empeño"),
    COMPRA("Compra"),
    AJUSTE("Ajuste"),
    OTRO("Otro");

    private final String displayName;

    SourceType(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }
}
