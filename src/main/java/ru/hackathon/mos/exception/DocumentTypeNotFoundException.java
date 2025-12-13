package ru.hackathon.mos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentTypeNotFoundException extends RuntimeException {

    public DocumentTypeNotFoundException(String message) {
        super(message);
    }
}