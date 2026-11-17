import { renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { useLockBodyScroll } from './useLockBodyScroll';

describe('useLockBodyScroll', () => {
  it('shouldLockAndRestoreMainOverflow', () => {
    const main = document.createElement('main');
    main.style.overflow = 'auto';
    document.body.appendChild(main);

    const { rerender, unmount } = renderHook(({ locked }) => useLockBodyScroll(locked), {
      initialProps: { locked: false },
    });

    rerender({ locked: true });
    expect(main.style.overflow).toBe('hidden');

    unmount();
    expect(main.style.overflow).toBe('auto');
    main.remove();
  });
});
