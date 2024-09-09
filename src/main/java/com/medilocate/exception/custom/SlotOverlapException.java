package com.medilocate.exception.custom;

public class SlotOverlapException extends RuntimeException {
    public SlotOverlapException() {
    }

    public SlotOverlapException(String message) {
        super(message);
    }
}
