package com.openlifeops.api.web;

import com.openlifeops.core.knowledge.IngestDocumentCommand;
import com.openlifeops.core.knowledge.KnowledgeDocument;
import com.openlifeops.knowledge.KnowledgeService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public final class DocumentUploadService {

    static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    private final KnowledgeService knowledgeService;
    private final InMemoryUploadedDocumentStore uploadedDocumentStore;

    public DocumentUploadService(KnowledgeService knowledgeService, InMemoryUploadedDocumentStore uploadedDocumentStore) {
        this.knowledgeService = knowledgeService;
        this.uploadedDocumentStore = uploadedDocumentStore;
    }

    public KnowledgeDocument ingest(String pack, String documentType, MultipartFile file) {
        validate(file);
        String fileName = requireSafeFileName(file.getOriginalFilename());
        byte[] bytes = read(file);
        ExtractedDocument extracted = extract(fileName, file.getContentType(), bytes);

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("ingestionMethod", "multipart-upload");
        attributes.put("sourceContentType", extracted.sourceContentType());
        attributes.put("sourceBytes", String.valueOf(bytes.length));
        attributes.put("extractedCharacters", String.valueOf(extracted.text().length()));
        attributes.put("extractionStatus", "SUCCESS");

        KnowledgeDocument document = knowledgeService.ingest(new IngestDocumentCommand(
                pack, documentType, fileName, extracted.indexedContentType(), extracted.text(), attributes));
        uploadedDocumentStore.put(document.getId(), bytes, extracted.sourceContentType(), extracted.text().length());
        return document;
    }

    private static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw DocumentUploadException.invalid("A non-empty document file is required.");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw DocumentUploadException.invalid("Document exceeds the 10 MB upload limit.");
        }
    }

    private static String requireSafeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw DocumentUploadException.invalid("Uploaded document must have a file name.");
        }
        String fileName = originalFileName.replace('\\', '/');
        if (fileName.contains("..")) {
            throw DocumentUploadException.invalid("Unsafe document file name.");
        }
        int lastSlash = fileName.lastIndexOf('/');
        return lastSlash >= 0 ? fileName.substring(lastSlash + 1) : fileName;
    }

    private static byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw DocumentUploadException.extractionFailed("Unable to read uploaded document.", exception);
        }
    }

    private static ExtractedDocument extract(String fileName, String suppliedContentType, byte[] bytes) {
        String normalizedType = suppliedContentType == null ? "" : suppliedContentType.toLowerCase(Locale.ROOT);
        if (hasPdfHeader(bytes)) {
            return extractPdf(bytes);
        }
        if (normalizedType.equals("application/pdf") || fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw DocumentUploadException.unsupported("The uploaded PDF does not have a valid PDF header.");
        }
        if (normalizedType.startsWith("text/plain") || fileName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            String text = new String(bytes, StandardCharsets.UTF_8).strip();
            if (text.isBlank()) {
                throw DocumentUploadException.extractionFailed("The uploaded text document contains no extractable text.", null);
            }
            return new ExtractedDocument(text, "text/plain", "text/plain");
        }
        throw DocumentUploadException.unsupported("Only text/plain and text-based application/pdf documents are supported.");
    }

    private static ExtractedDocument extractPdf(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                throw DocumentUploadException.unsupported("Password-protected PDFs are not supported.");
            }
            String text = new PDFTextStripper().getText(document).strip();
            if (text.isBlank()) {
                throw DocumentUploadException.extractionFailed(
                        "The PDF has no extractable text. OCR for scanned PDFs is not available yet.", null);
            }
            return new ExtractedDocument(text, "application/pdf", "text/plain");
        } catch (DocumentUploadException exception) {
            throw exception;
        } catch (IOException exception) {
            throw DocumentUploadException.extractionFailed("Unable to extract text from the uploaded PDF.", exception);
        }
    }

    private static boolean hasPdfHeader(byte[] bytes) {
        return bytes.length >= 5
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-';
    }

    private record ExtractedDocument(String text, String sourceContentType, String indexedContentType) {
    }
}
