package com.medilocate.exception.custom;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {}

    public UserNotFoundException(String message) {
        super(message);
    }

}
