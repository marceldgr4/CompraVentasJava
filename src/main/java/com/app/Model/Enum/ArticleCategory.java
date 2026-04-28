package com.app.Model.Enum;

public enum ArticleCategory {
    Electronico,
    Joyeria,
    Otros;

    public boolean requiresWeight(){
        return this  == Joyeria;
    }
}
