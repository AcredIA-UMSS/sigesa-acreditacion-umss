package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.UpdatePeerRoomChecklistUseCase;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.PeerRoomChecklistItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class UpdatePeerRoomChecklistService implements UpdatePeerRoomChecklistUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD", "EE");

    private final ProcessQueryPort processQueryPort;
    private final PeerRoomChecklistPort checklistPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UpdatePeerRoomChecklistService(
            ProcessQueryPort processQueryPort,
            PeerRoomChecklistPort checklistPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.checklistPort = checklistPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<PeerRoomChecklistItem> update(
            UUID processId,
            List<ChecklistUpdateItem> items,
            UUID actorId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        assertRole(roles);
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), actorId, roles, jwtProgramScope, assignmentRepository);

        if (items == null || items.isEmpty()) {
            throw new ValidationException("Debe enviar al menos un ítem de checklist.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (ChecklistUpdateItem update : items) {
            if (update.itemCode() == null || update.itemCode().isBlank()) {
                throw new ValidationException("itemCode es obligatorio.");
            }
            PeerRoomChecklistItem item = checklistPort.findByProcessIdAndItemCode(processId, update.itemCode())
                    .orElseGet(() -> seedMissing(processId, update.itemCode(), now));
            item.setChecked(update.checked());
            item.setCheckedBy(update.checked() ? actorId : null);
            item.setCheckedAt(update.checked() ? now : null);
            checklistPort.save(item);
        }
        return checklistPort.findByProcessId(processId);
    }

    private static PeerRoomChecklistItem seedMissing(UUID processId, String itemCode, LocalDateTime now) {
        PeerRoomChecklistItem.CatalogEntry catalog = PeerRoomChecklistItem.CATALOG.stream()
                .filter(entry -> entry.itemCode().equals(itemCode))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Ítem de checklist desconocido: " + itemCode));
        return PeerRoomChecklistItem.seed(processId, catalog, now);
    }

    private static void assertRole(List<String> roles) {
        String role = roles == null || roles.isEmpty() ? "" : roles.getFirst().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new InvalidRoleException("Solo [TD] o [EE] pueden actualizar el checklist.");
        }
    }
}
