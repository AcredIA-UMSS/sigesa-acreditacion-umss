import { ChevronRight } from 'lucide-react';
import { useEffect, useId, useState, type ReactNode } from 'react';

type LayerDepth = 1 | 2 | 3 | 4;

interface NormativeCollapsibleLayerProps {
  depth: LayerDepth;
  title: string;
  meta?: string;
  badge?: ReactNode;
  leadingIcon?: ReactNode;
  /** Controlled open state (optional). */
  open?: boolean;
  defaultOpen?: boolean;
  onOpenChange?: (open: boolean) => void;
  children: ReactNode;
  className?: string;
}

const depthStyles: Record<LayerDepth, { shell: string; header: string; body: string }> = {
  1: {
    shell: 'rounded-xl border border-primary-200 bg-body shadow-sm',
    header: 'bg-primary-50 hover:bg-primary-100 px-5 py-4',
    body: 'border-t border-gray-100 bg-body px-4 py-3 sm:px-5',
  },
  2: {
    shell: 'rounded-lg border border-gray-200 bg-gray-50/80',
    header: 'hover:bg-gray-100 px-4 py-3',
    body: 'border-t border-gray-100 bg-body px-3 py-3 sm:px-4',
  },
  3: {
    shell: 'rounded-lg border border-gray-200 bg-body',
    header: 'hover:bg-gray-50 px-4 py-3',
    body: 'border-t border-gray-100 bg-gray-50/40 px-3 py-3',
  },
  4: {
    shell: 'rounded-lg border border-gray-200 bg-body',
    header: 'hover:bg-gray-50 px-3 py-2.5',
    body: 'border-t border-gray-100 px-3 py-3',
  },
};

export function NormativeCollapsibleLayer({
  depth,
  title,
  meta,
  badge,
  leadingIcon,
  open: controlledOpen,
  defaultOpen = false,
  onOpenChange,
  children,
  className = '',
}: NormativeCollapsibleLayerProps) {
  const [internalOpen, setInternalOpen] = useState(defaultOpen);
  const isControlled = controlledOpen !== undefined;
  const open = isControlled ? controlledOpen : internalOpen;
  const panelId = useId();
  const styles = depthStyles[depth];

  useEffect(() => {
    if (!isControlled) {
      setInternalOpen(defaultOpen);
    }
  }, [defaultOpen, isControlled]);

  const toggle = () => {
    const next = !open;
    if (!isControlled) {
      setInternalOpen(next);
    }
    onOpenChange?.(next);
  };

  return (
    <div className={`${styles.shell} ${className}`.trim()}>
      <button
        type="button"
        aria-expanded={open}
        aria-controls={panelId}
        onClick={toggle}
        className={`flex w-full items-center gap-3 text-left transition-colors ${styles.header}`}
      >
        <span
          className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${
            depth === 1 ? 'bg-primary-600 text-body' : 'bg-gray-100 text-primary-700'
          }`}
          aria-hidden
        >
          <ChevronRight
            size={18}
            className={`transition-transform duration-200 ${open ? 'rotate-90' : ''}`}
          />
        </span>
        {leadingIcon && (
          <span className="shrink-0 text-primary-600" aria-hidden>
            {leadingIcon}
          </span>
        )}
        <span className="min-w-0 flex-1">
          <span className="block text-body-md font-semibold text-primary-800">{title}</span>
          {meta && <span className="mt-0.5 block text-label-md text-gray-600">{meta}</span>}
        </span>
        {badge && <span className="shrink-0">{badge}</span>}
      </button>
      {open && (
        <div id={panelId} className={styles.body}>
          {children}
        </div>
      )}
    </div>
  );
}
