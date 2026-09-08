import { ChevronDown, ExternalLink, GitBranch, Layers, ListChecks, Scale } from 'lucide-react';
import { useState } from 'react';
import type {
  NormativeIndicatorDto,
  NormativeLevel1NodeDto,
  NormativeLevel2NodeDto,
  NormativeLevel3NodeDto,
} from '../../../api/model';
import { SubphaseCollaborationSection } from '../../subphases/components/SubphaseCollaborationSection';

interface ProcessNormativeTreeProps {
  level1Nodes: NormativeLevel1NodeDto[];
  processId: string;
  canUploadEvidence?: boolean;
  canObserveEvidence?: boolean;
  canReviewEvidence?: boolean;
  canSubsanateEvidence?: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}

export function ProcessNormativeTree({
  level1Nodes,
  processId,
  canUploadEvidence = false,
  canObserveEvidence = false,
  canReviewEvidence = false,
  canSubsanateEvidence = false,
  onNavigateToIndicator,
}: ProcessNormativeTreeProps) {
  const sorted = [...level1Nodes].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));

  if (sorted.length === 0) {
    return (
      <p className="text-body-md text-gray-600">
        Este proceso no tiene nodos normativos registrados.
      </p>
    );
  }

  return (
    <div className="space-y-3">
      {sorted.map((node) => (
        <Level1Accordion
          key={node.id ?? node.name}
          node={node}
          processId={processId}
          canUploadEvidence={canUploadEvidence}
          canObserveEvidence={canObserveEvidence}
          canReviewEvidence={canReviewEvidence}
          canSubsanateEvidence={canSubsanateEvidence}
          onNavigateToIndicator={onNavigateToIndicator}
        />
      ))}
    </div>
  );
}

function Level1Accordion({
  node,
  processId,
  canUploadEvidence,
  canObserveEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  node: NormativeLevel1NodeDto;
  processId: string;
  canUploadEvidence: boolean;
  canObserveEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const [open, setOpen] = useState(true);
  const level2Nodes = [...(node.level2Nodes ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );
  const label = node.label ?? 'Nivel 1';

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-body shadow-sm">
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className="flex w-full items-center justify-between gap-3 bg-primary-50 px-5 py-4 text-left transition-colors hover:bg-primary-100"
      >
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-600 text-body">
            <Layers size={18} />
          </div>
          <div>
            <p className="text-heading-sm font-semibold text-primary-800">{node.name}</p>
            <p className="text-label-md text-gray-600">
              {label} {node.order ?? '—'} · {level2Nodes.length} subnodo
              {level2Nodes.length === 1 ? '' : 's'}
              {node.status && (
                <span className="ml-2 rounded-full bg-primary-100 px-2 py-0.5 text-label-md font-medium text-primary-800">
                  {node.status}
                </span>
              )}
            </p>
          </div>
        </div>
        <ChevronDown
          size={20}
          className={`text-primary-600 transition-transform ${open ? 'rotate-180' : ''}`}
        />
      </button>

      {open && (
        <div className="divide-y divide-gray-100 border-t border-gray-100">
          {level2Nodes.map((level2) => (
            <Level2Section
              key={level2.id ?? level2.name}
              node={level2}
              level1Name={node.name ?? label}
              processId={processId}
              canUploadEvidence={canUploadEvidence}
              canObserveEvidence={canObserveEvidence}
              canReviewEvidence={canReviewEvidence}
              canSubsanateEvidence={canSubsanateEvidence}
              onNavigateToIndicator={onNavigateToIndicator}
            />
          ))}
          {level2Nodes.length === 0 && (
            <p className="px-5 py-3 text-body-md text-gray-500">Sin subnodos</p>
          )}
        </div>
      )}
    </div>
  );
}

function Level2Section({
  node,
  level1Name,
  processId,
  canUploadEvidence,
  canObserveEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  node: NormativeLevel2NodeDto;
  level1Name: string;
  processId: string;
  canUploadEvidence: boolean;
  canObserveEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const [open, setOpen] = useState(true);
  const level3Nodes = [...(node.level3Nodes ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );
  const label = node.label ?? 'Nivel 2';

  return (
    <div className="bg-gray-50/50">
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className="flex w-full items-center justify-between gap-3 px-5 py-3 text-left hover:bg-gray-100"
      >
        <div className="flex items-center gap-2">
          <GitBranch size={16} className="text-primary-600" />
          <div>
            <p className="text-body-md font-semibold text-gray-800">{node.name}</p>
            <p className="text-label-md text-gray-500">
              {label} {node.order ?? '—'} · {level3Nodes.length} subnodo
              {level3Nodes.length === 1 ? '' : 's'}
            </p>
          </div>
        </div>
        <ChevronDown
          size={18}
          className={`text-gray-500 transition-transform ${open ? 'rotate-180' : ''}`}
        />
      </button>

      {open && (
        <div className="space-y-2 border-t border-gray-100 bg-body px-4 py-3">
          {level3Nodes.map((level3) => (
            <Level3Section
              key={level3.id ?? level3.name}
              node={level3}
              level1Name={level1Name}
              level2Name={node.name ?? label}
              processId={processId}
              canUploadEvidence={canUploadEvidence}
              canObserveEvidence={canObserveEvidence}
              canReviewEvidence={canReviewEvidence}
              canSubsanateEvidence={canSubsanateEvidence}
              onNavigateToIndicator={onNavigateToIndicator}
            />
          ))}
          {level3Nodes.length === 0 && (
            <p className="text-body-md text-gray-500">Sin subnodos</p>
          )}
        </div>
      )}
    </div>
  );
}

function Level3Section({
  node,
  level1Name,
  level2Name,
  processId,
  canUploadEvidence,
  canObserveEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  node: NormativeLevel3NodeDto;
  level1Name: string;
  level2Name: string;
  processId: string;
  canUploadEvidence: boolean;
  canObserveEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const indicators = [...(node.indicators ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );
  const label = node.label ?? 'Nivel 3';

  return (
    <div className="rounded-lg border border-gray-200 bg-gray-50/80 p-4">
      <p className="text-body-md font-semibold text-gray-800">{node.name}</p>
      <p className="text-label-md text-gray-500">
        {label} {node.order ?? '—'} · {indicators.length} indicador
        {indicators.length === 1 ? '' : 'es'}
      </p>
      <ul className="mt-3 space-y-3">
        {indicators.map((indicator) => (
          <IndicatorRow
            key={indicator.id ?? indicator.code}
            indicator={indicator}
            level1Name={level1Name}
            level2Name={level2Name}
            level3Name={node.name ?? label}
            processId={processId}
            canUploadEvidence={canUploadEvidence}
            canObserveEvidence={canObserveEvidence}
            canReviewEvidence={canReviewEvidence}
            canSubsanateEvidence={canSubsanateEvidence}
            onNavigateToIndicator={onNavigateToIndicator}
          />
        ))}
        {indicators.length === 0 && (
          <li className="text-body-md text-gray-500">Sin indicadores</li>
        )}
      </ul>
    </div>
  );
}

function IndicatorRow({
  indicator,
  level1Name,
  level2Name,
  level3Name,
  processId,
  canUploadEvidence,
  canObserveEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  onNavigateToIndicator,
}: {
  indicator: NormativeIndicatorDto;
  level1Name: string;
  level2Name: string;
  level3Name: string;
  processId: string;
  canUploadEvidence: boolean;
  canObserveEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  onNavigateToIndicator?: (indicatorId: string) => void;
}) {
  const anchorId = indicator.id ? `indicator-${indicator.id}` : undefined;
  const breadcrumb = [level1Name, level2Name, level3Name].filter(Boolean).join(' › ');

  return (
    <li
      id={anchorId}
      className="scroll-mt-24 rounded-lg border border-gray-200 bg-body px-4 py-3 transition-shadow"
    >
      <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-body-md font-medium text-gray-800">
            {indicator.code && (
              <span className="mr-2 rounded bg-primary-100 px-2 py-0.5 font-mono text-label-md text-primary-800">
                {indicator.code}
              </span>
            )}
            {indicator.description}
          </p>
          <p className="text-label-md text-gray-500">{breadcrumb}</p>
          {indicator.weight != null && (
            <p className="mt-1 flex items-center gap-1 text-label-md text-gray-600">
              <Scale size={14} aria-hidden />
              Ponderación: {indicator.weight}
            </p>
          )}
          {indicator.referenceUrl && (
            <a
              href={indicator.referenceUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-1 inline-flex items-center gap-1 text-body-md text-primary-600 hover:text-primary-800"
            >
              <ExternalLink size={14} />
              Referencia normativa
            </a>
          )}
          {indicator.status && (
            <span className="mt-2 inline-flex items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-label-md font-medium text-gray-700">
              <ListChecks size={14} aria-hidden />
              {indicator.status}
            </span>
          )}
        </div>
        <span className="text-label-md text-gray-500">Orden {indicator.order ?? '—'}</span>
      </div>

      {indicator.legacySubphaseId ? (
        <SubphaseCollaborationSection
          processId={processId}
          phaseName={level1Name}
          subphaseId={indicator.legacySubphaseId}
          subphaseName={indicator.description ?? indicator.code ?? 'Indicador'}
          canUpload={canUploadEvidence}
          canObserve={canObserveEvidence}
          canReview={canReviewEvidence}
          canSubsanate={canSubsanateEvidence}
        />
      ) : (
        indicator.id && onNavigateToIndicator && (
          <button
            type="button"
            onClick={() => onNavigateToIndicator(indicator.id!)}
            className="mt-3 text-body-md text-primary-600 hover:text-primary-800"
          >
            Ver detalle del indicador
          </button>
        )
      )}
    </li>
  );
}
