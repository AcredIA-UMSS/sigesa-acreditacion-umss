import { ExternalLink, Scale } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import type {
  NormativeIndicatorDto,
  NormativeLevel1NodeDto,
  NormativeLevel2NodeDto,
  NormativeLevel3NodeDto,
} from '../../../api/model';
import { NormativeCollapsibleLayer } from '../../../components/normative/NormativeCollapsibleLayer';
import { Level1CloseAction } from '../../level1/components/Level1CloseAction';
import {
  countIndicatorsUnderLevel1,
  countIndicatorsUnderLevel2,
  findNormativeIndicatorPath,
  sortByOrder,
} from '../lib/normativeTreeUtils';
import { NormativeIndicatorCollaborationSection } from './NormativeIndicatorCollaborationSection';

interface ProcessNormativeTreeProps {
  level1Nodes: NormativeLevel1NodeDto[];
  processId: string;
  canUploadEvidence?: boolean;
  canReviewEvidence?: boolean;
  canSubsanateEvidence?: boolean;
  canCloseLevel1?: boolean;
  onStructureUpdated?: () => void;
  onNavigateToIndicator?: (indicatorId: string) => void;
  /** Abre automáticamente la ruta hasta este indicador (p. ej. desde búsqueda). */
  expandToIndicatorId?: string;
}

export function ProcessNormativeTree({
  level1Nodes,
  processId,
  canUploadEvidence = false,
  canReviewEvidence = false,
  canSubsanateEvidence = false,
  canCloseLevel1 = false,
  onStructureUpdated,
  onNavigateToIndicator,
  expandToIndicatorId,
}: ProcessNormativeTreeProps) {
  const sorted = sortByOrder(level1Nodes);
  const [openLevel1, setOpenLevel1] = useState<Record<string, boolean>>({});
  const [openLevel2, setOpenLevel2] = useState<Record<string, boolean>>({});
  const [openLevel3, setOpenLevel3] = useState<Record<string, boolean>>({});
  const [openIndicators, setOpenIndicators] = useState<Record<string, boolean>>({});

  const expandPath = useMemo(
    () =>
      expandToIndicatorId
        ? findNormativeIndicatorPath(level1Nodes, expandToIndicatorId)
        : null,
    [expandToIndicatorId, level1Nodes],
  );

  useEffect(() => {
    if (!expandPath) return;

    setOpenLevel1((prev) => ({ ...prev, [expandPath.level1Id]: true }));
    setOpenLevel2((prev) => ({ ...prev, [expandPath.level2Id]: true }));
    setOpenLevel3((prev) => ({ ...prev, [expandPath.level3Id]: true }));
    setOpenIndicators((prev) => ({ ...prev, [expandPath.indicatorId]: true }));

    window.requestAnimationFrame(() => {
      document.getElementById(`indicator-${expandPath.indicatorId}`)?.scrollIntoView({
        behavior: 'smooth',
        block: 'center',
      });
    });
  }, [expandPath]);

  if (sorted.length === 0) {
    return (
      <p className="text-body-md text-gray-600">
        Este proceso no tiene nodos normativos registrados.
      </p>
    );
  }

  return (
    <div className="space-y-3">
      <p className="text-body-md text-gray-600">
        Se muestran solo las dimensiones (nivel 1). Use el icono ▸ en cada fila para desplegar
        áreas, criterios e indicadores según necesite.
      </p>
      {sorted.map((node) => {
        const level1Id = node.id ?? node.name ?? '';
        const level2Nodes = sortByOrder(node.level2Nodes ?? []);
        const label = node.label ?? 'Dimensión';
        const indicatorCount = countIndicatorsUnderLevel1(node);

        return (
          <NormativeCollapsibleLayer
            key={level1Id}
            depth={1}
            title={node.name ?? label}
            meta={`${label} ${node.order ?? '—'} · ${level2Nodes.length} subnivel(es) · ${indicatorCount} indicador(es)`}
            badge={
              node.status ? (
                <span className="rounded-full bg-primary-100 px-2 py-0.5 text-label-md font-medium text-primary-800">
                  {node.status}
                </span>
              ) : undefined
            }
            open={openLevel1[level1Id]}
            onOpenChange={(open) => setOpenLevel1((prev) => ({ ...prev, [level1Id]: open }))}
          >
            <div className="space-y-2">
              {level2Nodes.map((level2) => (
                <Level2Section
                  key={level2.id ?? level2.name}
                  node={level2}
                  level1Name={node.name ?? label}
                  open={Boolean(level2.id && openLevel2[level2.id])}
                  onOpenChange={(open) => {
                    if (!level2.id) return;
                    setOpenLevel2((prev) => ({ ...prev, [level2.id!]: open }));
                  }}
                  openLevel3={openLevel3}
                  onOpenLevel3Change={(level3Id, open) =>
                    setOpenLevel3((prev) => ({ ...prev, [level3Id]: open }))
                  }
                  openIndicators={openIndicators}
                  onOpenIndicatorChange={(indicatorId, open) =>
                    setOpenIndicators((prev) => ({ ...prev, [indicatorId]: open }))
                  }
                  canUploadEvidence={canUploadEvidence}
                  canReviewEvidence={canReviewEvidence}
                  canSubsanateEvidence={canSubsanateEvidence}
                  onNavigateToIndicator={onNavigateToIndicator}
                />
              ))}
              {level2Nodes.length === 0 && (
                <p className="text-body-md text-gray-500">Sin subnodos en esta dimensión.</p>
              )}
              {canCloseLevel1 && node.id && (
                <Level1CloseAction
                  processId={processId}
                  level1Id={node.id}
                  level1Name={node.name ?? label}
                  level1Label={label}
                  level1Status={node.status}
                  onCompleted={() => onStructureUpdated?.()}
                  onNavigateToIndicator={onNavigateToIndicator}
                />
              )}
            </div>
          </NormativeCollapsibleLayer>
        );
      })}
    </div>
  );
}

function Level2Section({
  node,
  level1Name,
  open,
  onOpenChange,
  openLevel3,
  onOpenLevel3Change,
  openIndicators,
  onOpenIndicatorChange,
  canUploadEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  node: NormativeLevel2NodeDto;
  level1Name: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  openLevel3: Record<string, boolean>;
  onOpenLevel3Change: (level3Id: string, open: boolean) => void;
  openIndicators: Record<string, boolean>;
  onOpenIndicatorChange: (indicatorId: string, open: boolean) => void;
  canUploadEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const level3Nodes = sortByOrder(node.level3Nodes ?? []);
  const label = node.label ?? 'Área';
  const indicatorCount = countIndicatorsUnderLevel2(node);

  return (
    <NormativeCollapsibleLayer
      depth={2}
      title={node.name ?? label}
      meta={`${label} ${node.order ?? '—'} · ${level3Nodes.length} subnivel(es) · ${indicatorCount} indicador(es)`}
      open={open}
      onOpenChange={onOpenChange}
    >
      <div className="space-y-2">
        {level3Nodes.map((level3) => (
          <Level3Section
            key={level3.id ?? level3.name}
            node={level3}
            level1Name={level1Name}
            level2Name={node.name ?? label}
            open={Boolean(level3.id && openLevel3[level3.id])}
            onOpenChange={(next) => {
              if (!level3.id) return;
              onOpenLevel3Change(level3.id, next);
            }}
            openIndicators={openIndicators}
            onOpenIndicatorChange={onOpenIndicatorChange}
            canUploadEvidence={canUploadEvidence}
            canReviewEvidence={canReviewEvidence}
            canSubsanateEvidence={canSubsanateEvidence}
            onNavigateToIndicator={onNavigateToIndicator}
          />
        ))}
        {level3Nodes.length === 0 && (
          <p className="text-body-md text-gray-500">Sin subnodos en esta área.</p>
        )}
      </div>
    </NormativeCollapsibleLayer>
  );
}

function Level3Section({
  node,
  level1Name,
  level2Name,
  open,
  onOpenChange,
  openIndicators,
  onOpenIndicatorChange,
  canUploadEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  node: NormativeLevel3NodeDto;
  level1Name: string;
  level2Name: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  openIndicators: Record<string, boolean>;
  onOpenIndicatorChange: (indicatorId: string, open: boolean) => void;
  canUploadEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const indicators = sortByOrder(node.indicators ?? []);
  const label = node.label ?? 'Criterio';

  return (
    <NormativeCollapsibleLayer
      depth={3}
      title={node.name ?? label}
      meta={`${label} ${node.order ?? '—'} · ${indicators.length} indicador(es)`}
      open={open}
      onOpenChange={onOpenChange}
    >
      <ul className="space-y-2">
        {indicators.map((indicator) => (
          <IndicatorRow
            key={indicator.id ?? indicator.code}
            indicator={indicator}
            level1Name={level1Name}
            level2Name={level2Name}
            level3Name={node.name ?? label}
            open={Boolean(indicator.id && openIndicators[indicator.id])}
            onOpenChange={(next) => {
              if (!indicator.id) return;
              onOpenIndicatorChange(indicator.id, next);
            }}
            canUploadEvidence={canUploadEvidence}
            canReviewEvidence={canReviewEvidence}
            canSubsanateEvidence={canSubsanateEvidence}
            onNavigateToIndicator={onNavigateToIndicator}
          />
        ))}
        {indicators.length === 0 && (
          <li className="text-body-md text-gray-500">Sin indicadores en este criterio.</li>
        )}
      </ul>
    </NormativeCollapsibleLayer>
  );
}

function IndicatorRow({
  indicator,
  level1Name,
  level2Name,
  level3Name,
  open,
  onOpenChange,
  canUploadEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  indicator: NormativeIndicatorDto;
  level1Name: string;
  level2Name: string;
  level3Name: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  canUploadEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const anchorId = indicator.id ? `indicator-${indicator.id}` : undefined;
  const breadcrumb = [level1Name, level2Name, level3Name].filter(Boolean).join(' › ');
  const title = indicator.code
    ? `${indicator.code} — ${indicator.description ?? 'Indicador'}`
    : (indicator.description ?? 'Indicador');

  return (
    <li id={anchorId} className="scroll-mt-24 list-none">
      <NormativeCollapsibleLayer
        depth={4}
        title={title}
        meta={breadcrumb}
        badge={
          indicator.status ? (
            <span className="rounded-full bg-gray-100 px-2 py-0.5 text-label-md font-medium text-gray-700">
              {indicator.status}
            </span>
          ) : undefined
        }
        open={open}
        onOpenChange={onOpenChange}
      >
        <div className="space-y-2">
          {indicator.weight != null && (
            <p className="flex items-center gap-1 text-label-md text-gray-600">
              <Scale size={14} aria-hidden />
              Ponderación: {indicator.weight}
            </p>
          )}
          {indicator.referenceUrl && (
            <a
              href={indicator.referenceUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-body-md text-primary-600 hover:text-primary-800"
            >
              <ExternalLink size={14} />
              Referencia normativa
            </a>
          )}
          {indicator.id && (
            <NormativeIndicatorCollaborationSection
              indicatorId={indicator.id}
              indicatorLabel={indicator.code ?? indicator.description ?? 'Indicador'}
              indicatorStatus={indicator.status}
              breadcrumb={breadcrumb}
              canUpload={canUploadEvidence}
              canSubsanate={canSubsanateEvidence}
              canReview={canReviewEvidence}
            />
          )}
          {onNavigateToIndicator && indicator.id && (
            <button
              type="button"
              onClick={() => onNavigateToIndicator(indicator.id!)}
              className="text-body-md text-primary-600 hover:text-primary-800"
            >
              Resaltar en la página
            </button>
          )}
        </div>
      </NormativeCollapsibleLayer>
    </li>
  );
}
