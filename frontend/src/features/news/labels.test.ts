import { describe, expect, it } from 'vitest';
import { formatNewsDate, NEWS_CATEGORIES, newsCategoryLabel, newsStatusLabel } from './labels';

describe('news/labels unit tests', () => {
  it('exposes standard news categories', () => {
    expect(NEWS_CATEGORIES).toEqual(['EVENTO', 'CAPACITACION', 'LOGRO']);
  });

  describe('newsCategoryLabel', () => {
    it('returns human readable labels for known categories', () => {
      expect(newsCategoryLabel('EVENTO')).toBe('Evento');
      expect(newsCategoryLabel('CAPACITACION')).toBe('Capacitación');
      expect(newsCategoryLabel('LOGRO')).toBe('Logro');
    });

    it('falls back to "Noticia" when category is undefined or empty', () => {
      expect(newsCategoryLabel(undefined)).toBe('Noticia');
      expect(newsCategoryLabel('')).toBe('Noticia');
    });

    it('returns the raw string for unknown categories', () => {
      expect(newsCategoryLabel('ACADEMICO')).toBe('ACADEMICO');
    });
  });

  describe('newsStatusLabel', () => {
    it('maps known statuses correctly', () => {
      expect(newsStatusLabel('BORRADOR')).toBe('Borrador');
      expect(newsStatusLabel('PUBLICADA')).toBe('Publicada');
    });

    it('falls back to "—" when undefined or empty', () => {
      expect(newsStatusLabel(undefined)).toBe('—');
      expect(newsStatusLabel('')).toBe('—');
    });

    it('returns the raw status when unknown', () => {
      expect(newsStatusLabel('ARCHIVADA')).toBe('ARCHIVADA');
    });
  });

  describe('formatNewsDate', () => {
    it('formats valid ISO date strings to es-BO format', () => {
      const formatted = formatNewsDate('2026-11-04T10:00:00Z');
      expect(formatted).toMatch(/04/);
      expect(formatted).toMatch(/2026/);
    });

    it('returns empty string for null or empty dates', () => {
      expect(formatNewsDate(undefined)).toBe('');
      expect(formatNewsDate('')).toBe('');
    });

    it('returns the raw input if date is invalid', () => {
      expect(formatNewsDate('fecha-invalida')).toBe('fecha-invalida');
    });
  });
});
