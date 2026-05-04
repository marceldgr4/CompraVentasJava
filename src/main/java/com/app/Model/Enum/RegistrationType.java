package com.app.Model.Enum;

public enum RegistrationType {
    COMPLETO,
    RAPIDO;
    public boolean isCompleto() {
        return this == (COMPLETO);
    }
}
