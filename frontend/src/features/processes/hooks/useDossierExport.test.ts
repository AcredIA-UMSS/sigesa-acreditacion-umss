import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useDossierExport } from './useDossierExport';
import * as customFetchModule from '../../../lib/api/customFetch';

vi.mock('../../../lib/api/customFetch', () => ({
  customFetch: vi.fn(),
}));

vi.mock('../../../lib/auth/authBridge', () => ({
  resolveAccessToken: vi.fn(),
}));

describe('useDossierExport', () => {
  const processId = '11111111-2222-3333-4444-555555555555';
  const jobId = '99999999-8888-7777-6666-555555555555';

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('initializes with default state', () => {
    const { result } = renderHook(() => useDossierExport(processId));

    expect(result.current.isPending).toBe(false);
    expect(result.current.progress).toBe(0);
    expect(result.current.error).toBeNull();
  });

  it('runs complete export and download flow successfully', async () => {
    const mockCustomFetch = vi.mocked(customFetchModule.customFetch);

    // POST enqueues job
    mockCustomFetch.mockResolvedValueOnce({
      data: {
        jobId,
        status: 'PENDING',
        message: 'Started',
        statusUrl: `/api/v1/processes/${processId}/dossier/export-jobs/${jobId}`,
      },
      status: 202,
      headers: new Headers(),
    });

    // First status poll: PROCESSING at 50%
    mockCustomFetch.mockResolvedValueOnce({
      data: {
        jobId,
        status: 'PROCESSING',
        progressPercentage: 50,
        downloadUrl: null,
        errorMessage: null,
      },
      status: 200,
      headers: new Headers(),
    });

    // Second status poll: COMPLETED at 100%
    mockCustomFetch.mockResolvedValueOnce({
      data: {
        jobId,
        status: 'COMPLETED',
        progressPercentage: 100,
        downloadUrl: `/api/v1/processes/${processId}/dossier/export-jobs/${jobId}/download`,
        errorMessage: null,
      },
      status: 200,
      headers: new Headers(),
    });

    // Mock fetch for file download
    const mockArrayBuffer = new ArrayBuffer(8);
    const mockResponse = {
      ok: true,
      headers: new Headers({
        'content-disposition': 'attachment; filename="Dossier_Test.zip"',
      }),
      arrayBuffer: vi.fn().mockResolvedValue(mockArrayBuffer),
    };
    const globalFetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(mockResponse as unknown as Response);

    const createObjectURLSpy = vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:mock-url');
    const revokeObjectURLSpy = vi.spyOn(window.URL, 'revokeObjectURL').mockImplementation(() => {});

    const { result } = renderHook(() => useDossierExport(processId));

    await act(async () => {
      await result.current.exportDossier();
    });

    expect(result.current.isPending).toBe(false);
    expect(result.current.progress).toBe(100);
    expect(result.current.error).toBeNull();
    expect(globalFetchSpy).toHaveBeenCalledWith(
      `/api/v1/processes/${processId}/dossier/export-jobs/${jobId}/download`,
      expect.anything(),
    );
    expect(createObjectURLSpy).toHaveBeenCalled();
    expect(revokeObjectURLSpy).toHaveBeenCalledWith('blob:mock-url');

    globalFetchSpy.mockRestore();
    createObjectURLSpy.mockRestore();
    revokeObjectURLSpy.mockRestore();
  });

  it('handles job failure during processing', async () => {
    const mockCustomFetch = vi.mocked(customFetchModule.customFetch);

    mockCustomFetch.mockResolvedValueOnce({
      data: {
        jobId,
        status: 'PENDING',
        message: 'Started',
        statusUrl: '',
      },
      status: 202,
      headers: new Headers(),
    });

    mockCustomFetch.mockResolvedValueOnce({
      data: {
        jobId,
        status: 'FAILED',
        progressPercentage: 20,
        downloadUrl: null,
        errorMessage: 'Fallo al comprimir archivos en S3',
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useDossierExport(processId));

    await act(async () => {
      await expect(result.current.exportDossier()).rejects.toThrow('Fallo al comprimir archivos en S3');
    });

    expect(result.current.isPending).toBe(false);
    expect(result.current.error).toBe('Fallo al comprimir archivos en S3');
  });

  it('handles API error when enqueuing job', async () => {
    const mockCustomFetch = vi.mocked(customFetchModule.customFetch);
    mockCustomFetch.mockRejectedValueOnce(new Error('Network Error'));

    const { result } = renderHook(() => useDossierExport(processId));

    await act(async () => {
      await expect(result.current.exportDossier()).rejects.toThrow('Network Error');
    });

    expect(result.current.isPending).toBe(false);
    expect(result.current.error).toBe('Network Error');
  });
});
