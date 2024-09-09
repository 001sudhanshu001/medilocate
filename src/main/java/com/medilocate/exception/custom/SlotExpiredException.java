package com.medilocate.exception.custom;


public class SlotExpiredException extends RuntimeException {

    public SlotExpiredException(String message) {
        super(message);
    }
}