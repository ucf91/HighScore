package com.company.common.exceptions;

public class InternalServerException extends RuntimeException {
    public InternalServerException(){
        super();
    }

    public InternalServerException(String msg){
        super(msg);
    }
}
