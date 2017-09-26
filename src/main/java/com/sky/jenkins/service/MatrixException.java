package com.sky.jenkins.service;

import com.sky.jenkins.json.MatrixError;

import javax.ws.rs.core.Response;

public class MatrixException extends RuntimeException {
    private String errorCode;
    private int httpStatusCode;

    public MatrixException(String message) {
        super(message);
    }

    public MatrixException(Response response) {
        super(response.readEntity(MatrixError.class).getMessage());

        MatrixError error = response.readEntity(MatrixError.class);
        this.errorCode = error.getErrorCode();
        this.httpStatusCode = response.getStatus();
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
