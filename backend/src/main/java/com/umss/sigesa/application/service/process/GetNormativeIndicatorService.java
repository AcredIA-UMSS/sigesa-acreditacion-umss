package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.NormativeIndicatorDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.port.in.GetNormativeIndicatorUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.model.NormativeIndicator;

import java.util.List;
import java.util.UUID;

public class GetNormativeIndicatorService implements GetNormativeIndicatorUseCase {

    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    public GetNormativeIndicatorService(NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        this.normativeHierarchyQueryPort = normativeHierarchyQueryPort;
    }

    @Override
    public NormativeIndicatorDetail getById(UUID indicatorId, ProcessQueryContext ctx) {
        NormativeHierarchyQueryPort.NormativeIndicatorContext context = normativeHierarchyQueryPort
                .findIndicatorContext(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        ProcessAccessPolicy.assertCanAccess(
                ctx.role(), context.careerId(), ctx.programScope(), context.processId());

        NormativeIndicator indicator = normativeHierarchyQueryPort.findIndicatorById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        List<String> path = normativeHierarchyQueryPort.findProcessTree(context.processId())
                .flatMap(tree -> NormativePathBuilder.buildPath(tree.level1Nodes(), indicatorId))
                .orElse(List.of(context.level1Name()));

        return new NormativeIndicatorDetail(
                context.processId(),
                context.careerId(),
                context.level1Id(),
                context.level1Name(),
                indicator,
                path
        );
    }
}
