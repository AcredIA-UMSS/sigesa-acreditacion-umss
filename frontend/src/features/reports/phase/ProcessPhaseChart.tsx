import { Fragment, useEffect, useState } from 'react';
import type { MethodologicalStageResponseDto } from '../../../api/model';
import {
  APPROVAL_LABEL,
  ARCU_STAGE_GUIDE,
  COST_GUIDE,
  START_DOCUMENTS,
  currentStage,
  type GuideStageContent,
} from './arcuPhaseGuide';
import { StageDeliverableFollowUp } from './StageDeliverableFollowUp';
import { StartDocuments } from './StartDocuments';

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'Pendiente',
  IN_PROGRESS: 'En curso',
  SUBMITTED_FOR_REVIEW: 'En revisión',
  OBSERVED: 'Observada',
  APPROVED: 'Aprobada',
};

function statusLabel(status?: string): string {
  return STATUS_LABEL[status ?? ''] ?? status ?? 'Pendiente';
}

function stageByCode(
  stages: MethodologicalStageResponseDto[],
  code: string,
): MethodologicalStageResponseDto | undefined {
  return stages.find((stage) => stage.code === code);
}

interface ProcessPhaseChartProps {
  processId: string;
  careerName: string;
  stages: MethodologicalStageResponseDto[];
  isLoading: boolean;
  errorMessage?: string;
}

export function ProcessPhaseChart({ processId, careerName, stages, isLoading, errorMessage }: ProcessPhaseChartProps) {
  const ordered = [...stages].sort((left, right) => (left.order ?? 0) - (right.order ?? 0));
  const active = currentStage(ordered);
  const [openId, setOpenId] = useState<string>('start');

  useEffect(() => {
    setOpenId(active?.id ?? active?.code ?? 'start');
  }, [active?.id, active?.code]);

  if (isLoading) {
    return <p className="text-body-md text-gray-600">Cargando las fases del proceso…</p>;
  }

  if (errorMessage) {
    return <p className="text-body-md text-danger">{errorMessage}</p>;
  }

  if (ordered.length === 0) {
    return <p className="text-body-md text-gray-600">Esta carrera todavía no tiene fases metodológicas.</p>;
  }

  const guide = ARCU_STAGE_GUIDE.map((item) => ({
    item,
    stage: stageByCode(ordered, item.code),
  })).filter((entry) => entry.stage);
  const externalPrep = stageByCode(ordered, 'EXTERNAL_PREP');
  const careerReady = externalPrep?.status === 'APPROVED';
  const budget = stageByCode(ordered, 'PREPARATORY')?.deliverables?.find(
    (deliverable) => deliverable.deliverableCode === 'BUDGET_FORECAST',
  );

  return (
    <section aria-label="Fase del proceso de acreditación">
      <p className="text-label-md uppercase text-gray-600">Fase actual</p>
      <h2 className="mt-1 text-heading-lg font-semibold text-primary-800">
        {guideTitle(active)} · {statusLabel(active?.status)}
      </h2>
      <p className="mt-1 text-body-md text-gray-600">
        {careerName} sigue la secuencia de autoevaluación ARCU-SUR. La marca indica la fase registrada en el proceso.
      </p>

      <ol className="mt-8 space-y-3 border-l border-gray-300 pl-4">
        <li>
          <FlowMarker
            title="Inicio"
            detail="Decisión institucional de postular la carrera a la acreditación ARCU-SUR."
            documents={START_DOCUMENTS}
            processId={processId}
            tone="done"
          />
        </li>

        {guide.map(({ item, stage }) => stage && (
          <Fragment key={stage.id ?? item.code}>
            {item.code === 'EXTERNAL_PREP' && (
              <li>
                <CostStep
                  budgetStatus={budget?.approvalStatus}
                  open={openId === 'costs'}
                  onToggle={() => setOpenId(openId === 'costs' ? '' : 'costs')}
                />
              </li>
            )}
            <li>
              <StageStep
                processId={processId}
                guide={item}
                stage={stage}
                isCurrent={stage.id === active?.id || stage.code === active?.code}
                open={openId === (stage.id ?? stage.code)}
                onToggle={() => {
                  const id = stage.id ?? stage.code ?? item.code;
                  setOpenId(openId === id ? '' : id);
                }}
              />
            </li>
            {item.code === 'EXTERNAL_PREP' && (
              <li>
                <FlowMarker
                  title="Carrera preparada para la evaluación externa"
                  detail={careerReady
                    ? 'La preparación de la evaluación externa ya fue aprobada.'
                    : 'Este hito se alcanza cuando se aprueba la preparación de la evaluación externa.'}
                  tone={careerReady ? 'done' : 'upcoming'}
                />
              </li>
            )}
          </Fragment>
        ))}

        <li>
          <FlowMarker
            title="Fin del alcance documentado en la guía"
            detail="La evaluación externa, el dictamen y la comunicación de resultados requieren la normativa oficial de ARCU-SUR."
            tone="upcoming"
          />
        </li>
      </ol>
    </section>
  );
}

function guideTitle(stage?: MethodologicalStageResponseDto): string {
  const match = ARCU_STAGE_GUIDE.find((item) => item.code === stage?.code);
  return match?.guideTitle ?? stage?.displayName ?? stage?.code ?? 'Fase del proceso';
}

function FlowMarker({
  title,
  detail,
  documents,
  processId,
  tone,
}: {
  title: string;
  detail: string;
  documents?: string[];
  processId?: string;
  tone: 'done' | 'upcoming';
}) {
  return (
    <div className={`rounded-xl border px-4 py-3 ${tone === 'done' ? 'border-success/40 bg-success/10' : 'border-gray-200 bg-gray-50'}`}>
      <p className="text-body-md font-semibold text-primary-800">{title}</p>
      <p className="mt-1 text-body-md text-gray-600">{detail}</p>
      {documents && documents.length > 0 && processId && (
        <StartDocuments processId={processId} documents={documents} />
      )}
    </div>
  );
}

function StageStep({
  processId,
  guide,
  stage,
  isCurrent,
  open,
  onToggle,
}: {
  processId: string;
  guide: GuideStageContent;
  stage: MethodologicalStageResponseDto;
  isCurrent: boolean;
  open: boolean;
  onToggle: () => void;
}) {
  const approved = stage.status === 'APPROVED';
  const shell = isCurrent
    ? 'border-primary-500 bg-primary-50'
    : approved
      ? 'border-success/40 bg-success/10'
      : 'border-gray-200 bg-body';

  return (
    <article className={`rounded-xl border ${shell}`}>
      <button
        type="button"
        className="flex w-full items-start justify-between gap-3 px-4 py-3 text-left"
        aria-expanded={open}
        aria-current={isCurrent ? 'step' : undefined}
        onClick={onToggle}
      >
        <span>
          <span className="block text-label-md font-medium text-gray-600">E{stage.order}</span>
          <span className="mt-1 block text-body-lg font-semibold text-primary-800">{guide.guideTitle}</span>
          {stage.displayName && stage.displayName !== guide.guideTitle && (
            <span className="mt-1 block text-body-md text-gray-600">En el proceso: {stage.displayName}</span>
          )}
        </span>
        <span className={`shrink-0 rounded-full border px-2.5 py-0.5 text-label-md font-medium ${badgeClass(stage.status, isCurrent)}`}>
          {statusLabel(stage.status)}
        </span>
      </button>
      {open && (
        <div className="border-t border-gray-200 px-4 py-3">
          <p className="text-label-md font-medium uppercase text-gray-600">Actividades de la guía</p>
          <ul className="mt-2 list-disc space-y-1 pl-5 text-body-md text-gray-800">
            {guide.activities.map((activity) => <li key={activity}>{activity}</li>)}
          </ul>
          <p className="mt-3 text-body-md text-gray-800">
            <span className="font-semibold">Resultado esperado: </span>
            {guide.result}
          </p>
          {guide.note && <p className="mt-2 text-body-md text-gray-600">{guide.note}</p>}
          {(stage.deliverables?.length ?? 0) > 0 && stage.id && (
            <StageDeliverableFollowUp
              processId={processId}
              stageId={stage.id}
              deliverables={stage.deliverables ?? []}
            />
          )}
        </div>
      )}
    </article>
  );
}

function CostStep({
  budgetStatus,
  open,
  onToggle,
}: {
  budgetStatus?: string;
  open: boolean;
  onToggle: () => void;
}) {
  const recorded = budgetStatus
    ? `Previsión presupuestaria del proceso: ${APPROVAL_LABEL[budgetStatus] ?? budgetStatus}.`
    : 'Todavía no hay una previsión presupuestaria registrada en la etapa preparatoria.';

  return (
    <article className="rounded-xl border border-dashed border-gray-300 bg-gray-50">
      <button type="button" className="flex w-full items-start justify-between gap-3 px-4 py-3 text-left" aria-expanded={open} onClick={onToggle}>
        <span>
          <span className="block text-label-md font-medium text-gray-600">Referencia de la guía</span>
          <span className="mt-1 block text-body-lg font-semibold text-primary-800">{COST_GUIDE.title}</span>
        </span>
        <span className="shrink-0 rounded-full border border-gray-300 bg-body px-2.5 py-0.5 text-label-md font-medium text-gray-700">
          Sin fase propia
        </span>
      </button>
      {open && (
        <div className="border-t border-gray-200 px-4 py-3">
          <ul className="list-disc space-y-1 pl-5 text-body-md text-gray-800">
            {COST_GUIDE.activities.map((activity) => <li key={activity}>{activity}</li>)}
          </ul>
          <p className="mt-3 text-body-md text-gray-800">
            <span className="font-semibold">Resultado esperado: </span>
            {COST_GUIDE.result}
          </p>
          <p className="mt-2 text-body-md text-gray-600">{COST_GUIDE.note}</p>
          <p className="mt-2 text-body-md text-gray-800">{recorded}</p>
        </div>
      )}
    </article>
  );
}

function badgeClass(status: string | undefined, isCurrent: boolean): string {
  if (isCurrent) return 'border-primary-300 bg-primary-100 text-primary-800';
  if (status === 'APPROVED') return 'border-success/30 bg-body text-success';
  if (status === 'OBSERVED') return 'border-secondary-300 bg-secondary-50 text-secondary-800';
  if (status === 'SUBMITTED_FOR_REVIEW') return 'border-warning/40 bg-warning/10 text-gray-800';
  return 'border-gray-300 bg-body text-gray-700';
}
