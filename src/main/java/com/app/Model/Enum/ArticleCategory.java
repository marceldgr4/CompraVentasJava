package com.app.Model.Enum;

public enum ArticleCategory {
    Electrodomesticos("Electrodomésticos"),
    Joyeria("Joyería"),
    Herramientas("Herramientas"),
    Tecnologia("Tecnología"),
    Otro("Otro");

    private final String displayName;

    ArticleCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public boolean requiresWeight(){
        return this == Joyeria;
    }
}
