package ru.hackathon.mos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WebCameraNotFoundException extends RuntimeException {
    public WebCameraNotFoundException(String message) {
        super(message);
    }
}
