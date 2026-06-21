package com.umss.sigesa.reports.security;

import com.umss.sigesa.reports.dto.FilterPayload;

public interface SecurityInjector {
    /**
     * Apply security constraints based on actor identity/claims.
     * May mutate and return a sanitized FilterPayload.
     * @param filter incoming filter from client
     * @param actor actor identifier (may be null or "unknown")
     * @return sanitized FilterPayload
     */
    FilterPayload apply(FilterPayload filter, String actor);
}
