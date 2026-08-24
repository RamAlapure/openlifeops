package com.openlifeops.core.domain;

public final class ValidationResult {

    private final boolean passed;
    private final String message;
    private final String evidenceId;

    private ValidationResult(boolean passed, String message, String evidenceId) {
        this.passed = passed;
        this.message = message;
        this.evidenceId = evidenceId;
    }

    public static ValidationResult pass(String message) {
        return new ValidationResult(true, message, null);
    }

    public static ValidationResult pass(String message, String evidenceId) {
        return new ValidationResult(true, message, evidenceId);
    }

    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message, null);
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public String getEvidenceId() {
        return evidenceId;
    }
}
