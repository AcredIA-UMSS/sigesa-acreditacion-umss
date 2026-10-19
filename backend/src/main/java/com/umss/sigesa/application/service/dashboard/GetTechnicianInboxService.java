package com.umss.sigesa.application.service.dashboard;

import com.umss.sigesa.application.model.dashboard.TechnicianInboxItem;
import com.umss.sigesa.application.port.in.GetTechnicianInboxUseCase;
import com.umss.sigesa.application.port.out.TechnicianInboxQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class GetTechnicianInboxService implements GetTechnicianInboxUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final TechnicianInboxQueryPort queryPort;

    public GetTechnicianInboxService(TechnicianInboxQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Page<TechnicianInboxItem> getInbox(UUID programId, UUID level1Id, String status, Pageable pageable) {
        Pageable effectivePageable = pageable;
        if (pageable.isPaged() && pageable.getPageSize() > MAX_PAGE_SIZE) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return queryPort.findInboxItems(programId, level1Id, status, effectivePageable);
    }
}
