import { useEffect } from 'react';

const SCROLL_LOCK_SELECTOR = 'main[data-app-scroll]';

/**
 * Congela los contenedores de scroll de la app (p. ej. `<main>`) mientras un
 * overlay está abierto. El scroll de página vive en `main`, no en `document.body`.
 */
export function useLockBodyScroll(locked: boolean): void {
  useEffect(() => {
    if (!locked || typeof document === 'undefined') {
      return;
    }

    const scrollContainers = Array.from(
      document.querySelectorAll<HTMLElement>(SCROLL_LOCK_SELECTOR),
    );

    if (scrollContainers.length === 0) {
      scrollContainers.push(
        ...Array.from(document.querySelectorAll<HTMLElement>('main')),
      );
    }

    const saved = scrollContainers.map((el) => ({
      el,
      overflow: el.style.overflow,
      scrollTop: el.scrollTop,
    }));

    scrollContainers.forEach((el) => {
      el.style.overflow = 'hidden';
    });

    return () => {
      saved.forEach(({ el, overflow, scrollTop }) => {
        el.style.overflow = overflow;
        el.scrollTop = scrollTop;
      });
    };
  }, [locked]);
}
