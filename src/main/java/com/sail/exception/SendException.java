package com.sail.exception;

public class SendException extends Exception{

    public SendException(String s) {
        super(s);
    }

    public SendException(String s, Exception e) {
        super(s, e);
    }
}
