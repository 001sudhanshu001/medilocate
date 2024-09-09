package com.medilocate.exception.custom;

public class AppointmentAlreadyStartedException extends RuntimeException {

    public AppointmentAlreadyStartedException(String message) {
        super(message);
    }
}