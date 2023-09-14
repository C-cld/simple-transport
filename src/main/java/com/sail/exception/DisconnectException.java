package com.sail.exception;

public class DisconnectException extends Exception{
    public DisconnectException(String s) {
        super(s);
    }

    public DisconnectException(String s, Exception e) {
        super(s, e);
    }
}
