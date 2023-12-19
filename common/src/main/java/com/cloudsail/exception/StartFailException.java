package com.cloudsail.exception;

public class StartFailException extends Exception{
    public StartFailException(String s) {
        super(s);
    }

    public StartFailException(String s, Exception e) {
        super(s, e);
    }
}
