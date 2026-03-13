package org.example.technicaldocs;

import org.example.technicaldocs.config.TechnicalDocsConfig;
import org.example.technicaldocs.model.TechnicalDocument;
import org.example.technicaldocs.model.TechnicalDocumentChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TechnicalDocumentChunker {

    private static final int MAX_MARKDOWN_HEADING_LEVEL = 6;
    private static final Pattern BLANK_LINE_SPLIT = Pattern.compile("\\R\\s*\\R+");
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    private final TechnicalDocsConfig config;

    public TechnicalDocumentChunker(TechnicalDocsConfig config) {
        this.config = config;
    }

    public List<TechnicalDocumentChunk> chunkDocument(TechnicalDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Technical document cannot be null");
        }

        if (document.getDocumentName() == null || document.getDocumentName().isBlank()) {
            throw new IllegalArgumentException("Technical document name cannot be null or blank");
        }

        if (document.getExtension() == null || document.getExtension().isBlank()) {
            throw new IllegalArgumentException("Technical document extension cannot be null or blank");
        }

        if (document.getContent() == null || document.getContent().isBlank()) {
            throw new IllegalArgumentException("Technical document content cannot be null or blank");
        }

        String extension = document.getExtension().trim().toLowerCase();

        List<TechnicalDocumentChunk> chunks = switch (extension) {
            case ".md" -> chunkMarkdownDocument(document);
            case ".txt" -> chunkTextDocument(document);
            default -> throw new IllegalArgumentException(
                    "Unsupported technical document extension: " + document.getExtension()
            );
        };

        if (chunks.isEmpty()) {
            throw new IllegalStateException(
                    "Chunking produced no chunks for document: " + document.getDocumentName()
            );
        }

        return chunks;
    }

    private List<TechnicalDocumentChunk> chunkMarkdownDocument(TechnicalDocument document) {
        List<TechnicalDocumentChunk> chunks = new ArrayList<>();
        String normalized = normalize(document.getContent());

        List<MarkdownSection> topSections = splitMarkdownByHeadingLevel(normalized, 1);

        if (topSections.isEmpty()) {
            addFallbackChunks(
                    document,
                    chunks,
                    document.getDocumentName(),
                    normalized
            );
            return chunks;
        }

        int[] chunkIndex = {0};

        for (MarkdownSection section : topSections) {
            processMarkdownSection(
                    document,
                    chunks,
                    section.title(),
                    section.body(),
                    1,
                    chunkIndex
            );
        }

        return chunks;
    }

    private void processMarkdownSection(
            TechnicalDocument document,
            List<TechnicalDocumentChunk> chunks,
            String headingPath,
            String body,
            int currentLevel,
            int[] chunkIndex
    ) {
        String rendered = renderChunkContent(headingPath, body);

        if (rendered.length() <= config.getMaxChunkLengthChars()) {
            chunks.add(new TechnicalDocumentChunk(
                    document.getDocumentName(),
                    document.getExtension(),
                    headingPath,
                    chunkIndex[0]++,
                    rendered
            ));
            return;
        }

        if (currentLevel < MAX_MARKDOWN_HEADING_LEVEL) {
            List<MarkdownSection> childSections = splitMarkdownByHeadingLevel(body, currentLevel + 1);

            if (!childSections.isEmpty()) {
                for (MarkdownSection child : childSections) {
                    String childHeadingPath = headingPath + " > " + child.title();
                    processMarkdownSection(
                            document,
                            chunks,
                            childHeadingPath,
                            child.body(),
                            currentLevel + 1,
                            chunkIndex
                    );
                }
                return;
            }
        }

        addFallbackChunks(document, chunks, headingPath, body, chunkIndex);
    }

    private List<TechnicalDocumentChunk> chunkTextDocument(TechnicalDocument document) {
        List<TechnicalDocumentChunk> chunks = new ArrayList<>();
        int[] chunkIndex = {0};

        addFallbackChunks(
                document,
                chunks,
                document.getDocumentName(),
                normalize(document.getContent()),
                chunkIndex
        );

        return chunks;
    }

    private void addFallbackChunks(
            TechnicalDocument document,
            List<TechnicalDocumentChunk> chunks,
            String headingPath,
            String rawText
    ) {
        int[] chunkIndex = {0};
        addFallbackChunks(document, chunks, headingPath, rawText, chunkIndex);
    }

    private void addFallbackChunks(
            TechnicalDocument document,
            List<TechnicalDocumentChunk> chunks,
            String headingPath,
            String rawText,
            int[] chunkIndex
    ) {
        String normalizedText = normalize(rawText);

        int maxBodyLength = computeMaxBodyLength(headingPath);
        if (maxBodyLength <= 0) {
            throw new IllegalStateException(
                    "Configured max chunk length is too small for heading path metadata"
            );
        }

        List<String> paragraphBasedChunks =
                splitByParagraphsWithFallbackToSentences(normalizedText, maxBodyLength);

        for (String chunkBody : paragraphBasedChunks) {
            String rendered = renderChunkContent(headingPath, chunkBody);

            if (rendered.length() > config.getMaxChunkLengthChars()) {
                throw new IllegalStateException(
                        "Fallback chunk exceeds max length for document: " + document.getDocumentName()
                );
            }

            chunks.add(new TechnicalDocumentChunk(
                    document.getDocumentName(),
                    document.getExtension(),
                    headingPath,
                    chunkIndex[0]++,
                    rendered
            ));
        }
    }

    private List<String> splitByParagraphsWithFallbackToSentences(String text, int maxBodyLength) {
        List<String> chunks = new ArrayList<>();
        List<String> paragraphs = extractParagraphs(text);

        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (paragraph.length() > maxBodyLength) {
                flushCurrentChunk(chunks, current);

                List<String> sentenceChunks = splitLongTextBySentences(paragraph, maxBodyLength);
                chunks.addAll(sentenceChunks);
                continue;
            }

            String candidate = current.isEmpty()
                    ? paragraph
                    : current + "\n\n" + paragraph;

            if (candidate.length() <= maxBodyLength) {
                if (!current.isEmpty()) {
                    current.append("\n\n");
                }
                current.append(paragraph);
            } else {
                String emitted = current.toString().trim();
                if (!emitted.isBlank()) {
                    chunks.add(emitted);
                }

                String overlap = buildOverlap(emitted, maxBodyLength);
                current = new StringBuilder();

                if (!overlap.isBlank()) {
                    String overlappedCandidate = overlap + "\n\n" + paragraph;
                    if (overlappedCandidate.length() <= maxBodyLength) {
                        current.append(overlappedCandidate);
                    } else {
                        current.append(paragraph);
                    }
                } else {
                    current.append(paragraph);
                }
            }
        }

        flushCurrentChunk(chunks, current);
        return chunks;
    }

    private List<String> splitLongTextBySentences(String text, int maxBodyLength) {
        List<String> chunks = new ArrayList<>();
        List<String> sentences = extractSentences(text);

        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (sentence.length() > maxBodyLength) {
                flushCurrentChunk(chunks, current);

                List<String> hardSplitChunks = hardSplitByCharacters(sentence, maxBodyLength);
                chunks.addAll(hardSplitChunks);
                continue;
            }

            String candidate = current.isEmpty()
                    ? sentence
                    : current + " " + sentence;

            if (candidate.length() <= maxBodyLength) {
                if (!current.isEmpty()) {
                    current.append(" ");
                }
                current.append(sentence);
            } else {
                String emitted = current.toString().trim();
                if (!emitted.isBlank()) {
                    chunks.add(emitted);
                }

                String overlap = buildOverlap(emitted, maxBodyLength);
                current = new StringBuilder();

                if (!overlap.isBlank()) {
                    String overlappedCandidate = overlap + " " + sentence;
                    if (overlappedCandidate.length() <= maxBodyLength) {
                        current.append(overlappedCandidate);
                    } else {
                        current.append(sentence);
                    }
                } else {
                    current.append(sentence);
                }
            }
        }

        flushCurrentChunk(chunks, current);
        return chunks;
    }

    private List<String> hardSplitByCharacters(String text, int maxBodyLength) {
        List<String> chunks = new ArrayList<>();
        int overlap = Math.min(config.getFallbackOverlapChars(), Math.max(0, maxBodyLength / 4));

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxBodyLength, text.length());
            String chunk = text.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end >= text.length()) {
                break;
            }

            start = Math.max(end - overlap, start + 1);
        }

        return chunks;
    }

    private List<MarkdownSection> splitMarkdownByHeadingLevel(String text, int level) {
        List<MarkdownSection> sections = new ArrayList<>();
        List<String> lines = Arrays.asList(normalize(text).split("\\R", -1));

        String headingPrefix = "#".repeat(level) + " ";

        String currentTitle = null;
        StringBuilder currentBody = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith(headingPrefix) && !line.startsWith(headingPrefix + "#")) {
                if (currentTitle != null) {
                    sections.add(new MarkdownSection(
                            currentTitle,
                            currentBody.toString().trim()
                    ));
                }

                currentTitle = line.substring(headingPrefix.length()).trim();
                currentBody = new StringBuilder();
            } else {
                if (currentTitle != null) {
                    if (!currentBody.isEmpty()) {
                        currentBody.append("\n");
                    }
                    currentBody.append(line);
                }
            }
        }

        if (currentTitle != null) {
            sections.add(new MarkdownSection(
                    currentTitle,
                    currentBody.toString().trim()
            ));
        }

        return sections;
    }

    private List<String> extractParagraphs(String text) {
        String normalized = normalize(text).trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        String[] parts = BLANK_LINE_SPLIT.split(normalized);
        List<String> paragraphs = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                paragraphs.add(trimmed);
            }
        }

        return paragraphs;
    }

    private List<String> extractSentences(String text) {
        String normalized = normalize(text).trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        String[] parts = SENTENCE_SPLIT.split(normalized);
        List<String> sentences = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                sentences.add(trimmed);
            }
        }

        return sentences;
    }

    private String buildOverlap(String previousChunkBody, int maxBodyLength) {
        if (previousChunkBody == null || previousChunkBody.isBlank()) {
            return "";
        }

        int overlapChars = Math.min(config.getFallbackOverlapChars(), maxBodyLength / 2);
        if (overlapChars <= 0) {
            return "";
        }

        String trimmed = previousChunkBody.trim();
        if (trimmed.length() <= overlapChars) {
            return trimmed;
        }

        return trimmed.substring(trimmed.length() - overlapChars).trim();
    }

    private void flushCurrentChunk(List<String> chunks, StringBuilder current) {
        String emitted = current.toString().trim();
        if (!emitted.isBlank()) {
            chunks.add(emitted);
        }
        current.setLength(0);
    }

    private String renderChunkContent(String headingPath, String body) {
        String normalizedBody = normalize(body).trim();

        String prefix = "Heading Path: " + headingPath.trim();
        if (normalizedBody.isBlank()) {
            return prefix;
        }

        return prefix + "\n\n" + normalizedBody;
    }

    private int computeMaxBodyLength(String headingPath) {
        String prefix = "Heading Path: " + headingPath.trim();
        return config.getMaxChunkLengthChars() - prefix.length() - 2;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
    }

    private record MarkdownSection(String title, String body) {
    }
}