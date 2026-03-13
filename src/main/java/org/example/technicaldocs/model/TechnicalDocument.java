package org.example.technicaldocs.model;

public class TechnicalDocument {

    private final String documentName;
    private final String extension;
    private final String content;

    public TechnicalDocument(String documentName, String extension, String content) {
        this.documentName = documentName;
        this.extension = extension;
        this.content = content;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getExtension() {
        return extension;
    }

    public String getContent() {
        return content;
    }
}