package com.umss.sigesa.domain.model;

public enum ReportFormat {
    XLSX,
    CSV,
    PDF;

    public static ReportFormat fromString(String value) {
        if (value == null) return XLSX;
        try {
            return ReportFormat.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return XLSX;
        }
    }
}
