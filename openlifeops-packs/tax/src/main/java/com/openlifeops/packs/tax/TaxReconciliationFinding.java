package com.openlifeops.packs.tax;

import java.util.List;

public record TaxReconciliationFinding(
        TaxFindingType type,
        String field,
        String message,
        List<TaxDocumentFacts> sources) {

    public TaxReconciliationFinding {
        sources = List.copyOf(sources);
    }
}
