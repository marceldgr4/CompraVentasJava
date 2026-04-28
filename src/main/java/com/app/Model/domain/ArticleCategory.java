package com.app.Model.domain;

public enum ArticleCategory {
    Electronico,
    Joyeria,
    Otros;

    public boolean requiresWeight(){
        return this  == Joyeria;
    }
}
