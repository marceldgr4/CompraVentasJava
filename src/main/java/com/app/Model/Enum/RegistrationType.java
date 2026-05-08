package com.app.Model.Enum;

import java.util.spi.ToolProvider;

public enum RegistrationType {
    COMPLETO,
    RAPIDO;
    public static ToolProvider FULL;

    public boolean isCompleto() {
        return this == (COMPLETO);
    }
}
