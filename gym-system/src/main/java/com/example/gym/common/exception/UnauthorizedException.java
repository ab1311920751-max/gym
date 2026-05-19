package com.example.gym.common.exception;

public class UnauthorizedException extends RuntimeException {

    private final String code;

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }

    public UnauthorizedException(String message) {
        super(message);
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }

    public String getCode() {
        return code;
    }
}
