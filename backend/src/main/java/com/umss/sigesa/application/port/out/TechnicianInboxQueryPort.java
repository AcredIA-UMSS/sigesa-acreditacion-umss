package com.umss.sigesa.application.port.out;

import com.umss.sigesa.application.model.dashboard.TechnicianInboxItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TechnicianInboxQueryPort {

    Page<TechnicianInboxItem> findInboxItems(UUID programId, UUID level1Id, String status, Pageable pageable);
}
