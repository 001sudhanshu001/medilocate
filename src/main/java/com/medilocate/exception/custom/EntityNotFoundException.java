package com.medilocate.exception.custom;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException() {}

    public EntityNotFoundException(String message) {
        super(message);
    }
}
