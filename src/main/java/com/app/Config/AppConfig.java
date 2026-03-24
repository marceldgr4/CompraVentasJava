package com.app.Config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .ignoreIfMissing()
            .load();

    public  static String get(String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()){
            throw new RuntimeException("Variable de entorno '"+ key + "' no encontrado " + "verificar tu archivo .env");
        }
        return value;
    }

}
