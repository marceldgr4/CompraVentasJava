package com.app.Service.exceptions;

/**
 * Excepción lanzada cuando ocurre un error de autenticación o autorización.
 */
public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
