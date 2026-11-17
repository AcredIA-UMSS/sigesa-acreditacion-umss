package com.umss.sigesa.domain.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PublicCertificateText {

    private static final DateTimeFormatter ES_LONG =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));

    private PublicCertificateText() {
    }

    public static String certificationSentence(PublicProgramSnapshot snapshot) {
        StringBuilder text = new StringBuilder();
        text.append("Se certifica que la carrera de ")
                .append(snapshot.programName())
                .append(" ha sido acreditada");
        if (hasText(snapshot.agency())) {
            text.append(" por ").append(snapshot.agency().trim());
        }
        text.append(", con vigencia del ")
                .append(formatDate(snapshot.validFrom()))
                .append(" al ")
                .append(formatDate(snapshot.validUntil()));
        if (hasText(snapshot.resolutionNumber())) {
            text.append(", según Resolución N.º ").append(snapshot.resolutionNumber().trim());
        }
        text.append(".");
        return text.toString();
    }

    public static String formatDate(LocalDate date) {
        return ES_LONG.format(date);
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
