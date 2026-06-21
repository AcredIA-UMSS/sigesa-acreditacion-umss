package com.umss.sigesa.reports.security;

import com.umss.sigesa.reports.dto.FilterPayload;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SimpleSecurityInjector implements SecurityInjector {

    /**
     * Actor format accepted (simple):
     * - "CC:<id1>[,<id2>]"  => career coordinator restricted to listed career ids
     * - "JD:ALL"            => dean/jd allowed all
     * - null or "unknown"   => no enforcement (dev mode)
     */
    @Override
    public FilterPayload apply(FilterPayload filter, String actor) {
        if (actor == null || actor.isBlank() || "unknown".equalsIgnoreCase(actor)) {
            return filter; // no-op in dev mode
        }

        // Simple parsing
        String[] parts = actor.split(":", 2);
        if (parts.length < 2) return filter;
        String role = parts[0];
        String scope = parts[1];

        if ("CC".equalsIgnoreCase(role)) {
            // scope should be comma-separated career ids, e.g. "123" or "123,456"
            List<Long> allowed = parseIds(scope);
            if (filter == null) filter = new FilterPayload();

            if (filter.getCareerIds() != null && !filter.getCareerIds().isEmpty()) {
                // ensure requested careers are subset of allowed
                boolean allAllowed = filter.getCareerIds().stream().allMatch(allowed::contains);
                if (!allAllowed) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Actor not authorized for requested career ids");
                }
                // nothing to change
            } else {
                // inject allowed career ids
                filter.setCareerIds(new ArrayList<>(allowed));
            }
            return filter;
        }

        // other roles: JD => allow all, default no-op
        return filter;
    }

    private List<Long> parseIds(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
