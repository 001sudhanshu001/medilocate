package com.medilocate.exception.custom;

public class EmailDuplicateException extends RuntimeException {
    public EmailDuplicateException() {
    }

    public EmailDuplicateException(String message) {
        super(message);
    }
}
