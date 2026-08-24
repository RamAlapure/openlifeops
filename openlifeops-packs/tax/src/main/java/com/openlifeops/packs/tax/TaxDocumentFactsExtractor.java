package com.openlifeops.packs.tax;

import com.openlifeops.core.knowledge.KnowledgeHit;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Deterministic text extractor for the Phase 6 text-first Form 16 workflow. */
public final class TaxDocumentFactsExtractor {

    private static final Pattern PAN = Pattern.compile("(?i)\\bPAN\\s*[:#-]?\\s*([A-Z]{5}[0-9]{4}[A-Z])");
    private static final Pattern YEAR = Pattern.compile(
            "(?i)\\b(?:financial year|fy|assessment year|ay)\\s*[:#-]?\\s*(20\\d{2}\\s*[-/]\\s*\\d{2})");
    private static final Pattern EMPLOYER = Pattern.compile("(?i)\\b(?:employer|company|deductor)\\s*[:#-]\\s*([^\\n.;]+)");
    private static final Pattern INCOME = Pattern.compile(
            "(?i)\\b(?:income from salary|gross salary|taxable income)\\s*(?:is|:)?\\s*(?:₹|rs\\.?|inr)?\\s*([0-9][0-9,]*)");
    private static final Pattern TDS = Pattern.compile(
            "(?i)\\b(?:tds(?: deducted)?|tax deducted at source)\\s*(?:is|:)?\\s*(?:₹|rs\\.?|inr)?\\s*([0-9][0-9,]*)");

    public TaxDocumentFacts extract(KnowledgeHit hit) {
        String content = hit.getChunk().getContent();
        return TaxDocumentFacts.from(
                hit,
                match(PAN, content),
                match(YEAR, content),
                match(EMPLOYER, content),
                amount(INCOME, content),
                amount(TDS, content));
    }

    private static String match(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).strip().toUpperCase(Locale.ROOT) : null;
    }

    private static Long amount(Pattern pattern, String content) {
        String value = match(pattern, content);
        return value == null ? null : Long.valueOf(value.replace(",", ""));
    }
}
