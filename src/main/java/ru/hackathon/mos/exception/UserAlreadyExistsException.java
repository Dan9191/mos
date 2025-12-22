package ru.hackathon.mos.exception;

/**
 * Ошибка создания пользователя.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
