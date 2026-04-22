package com.app.Service.exceptions;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio.
 * Por ejemplo: intentar vender un artículo sin stock, o registrar un empeño
 * con fechas inválidas.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
