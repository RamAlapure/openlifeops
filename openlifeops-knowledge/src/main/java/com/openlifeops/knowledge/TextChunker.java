package com.openlifeops.knowledge;

import java.util.ArrayList;
import java.util.List;

final class TextChunker {

    private static final int MAX_CHUNK_LENGTH = 800;

    private TextChunker() {
    }

    static List<String> chunk(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        String normalized = content.strip();
        String[] paragraphs = normalized.split("\\R\\R+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String piece = paragraph.strip();
            if (piece.isEmpty()) {
                continue;
            }
            if (current.length() + piece.length() + 2 > MAX_CHUNK_LENGTH) {
                if (!current.isEmpty()) {
                    chunks.add(current.toString().strip());
                    current = new StringBuilder();
                }
                if (piece.length() <= MAX_CHUNK_LENGTH) {
                    current.append(piece);
                } else {
                    chunks.addAll(splitLongText(piece));
                }
            } else {
                if (!current.isEmpty()) {
                    current.append("\n\n");
                }
                current.append(piece);
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }
        return List.copyOf(chunks);
    }

    private static List<String> splitLongText(String text) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());
            parts.add(text.substring(start, end).strip());
            start = end;
        }
        return parts;
    }
}
