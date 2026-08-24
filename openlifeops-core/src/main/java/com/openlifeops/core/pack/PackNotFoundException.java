package com.openlifeops.core.pack;

public class PackNotFoundException extends RuntimeException {

    public PackNotFoundException(String packId) {
        super("Pack not found: " + packId);
    }
}
