package com.company.infrastructure;

public class BootException extends RuntimeException {
    public BootException(){
        super();
    }

    public BootException(String message){
        super(message);
    }
}
