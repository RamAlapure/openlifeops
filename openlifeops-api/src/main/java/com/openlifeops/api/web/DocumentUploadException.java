package com.openlifeops.api.web;

import org.springframework.http.HttpStatus;

public final class DocumentUploadException extends RuntimeException {

    private final HttpStatus status;

    private DocumentUploadException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static DocumentUploadException invalid(String message) {
        return new DocumentUploadException(HttpStatus.BAD_REQUEST, message, null);
    }

    public static DocumentUploadException unsupported(String message) {
        return new DocumentUploadException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, null);
    }

    public static DocumentUploadException extractionFailed(String message, Throwable cause) {
        return new DocumentUploadException(HttpStatus.UNPROCESSABLE_CONTENT, message, cause);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
