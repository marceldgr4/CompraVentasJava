package com.app.Model.Enum;

public enum ArticleCategory {

    Electrodomesticos, Joyeria, Herramientas, Tecnologia, Otro;

    public boolean requiresWeight(){
        return this  == Joyeria;
    }
}
