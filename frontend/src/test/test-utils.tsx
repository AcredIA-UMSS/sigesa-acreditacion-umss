import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, type RenderOptions } from '@testing-library/react';
import type { ReactElement, ReactNode } from 'react';
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom';
import { AuthProvider } from '../lib/auth/AuthProvider';

export function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
}

interface AppRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  route?: string;
  routerProps?: Omit<MemoryRouterProps, 'children'>;
  queryClient?: QueryClient;
  withAuth?: boolean;
}

function createWrapper(options: AppRenderOptions) {
  const queryClient = options.queryClient ?? createTestQueryClient();
  const initialEntries = options.routerProps?.initialEntries ?? [options.route ?? '/'];
  const withAuth = options.withAuth ?? true;

  return function Wrapper({ children }: { children: ReactNode }) {
    const routed = (
      <MemoryRouter {...options.routerProps} initialEntries={initialEntries}>
        {children}
      </MemoryRouter>
    );

    return (
      <QueryClientProvider client={queryClient}>
        {withAuth ? <AuthProvider>{routed}</AuthProvider> : routed}
      </QueryClientProvider>
    );
  };
}

export function renderWithProviders(ui: ReactElement, options: AppRenderOptions = {}) {
  const queryClient = options.queryClient ?? createTestQueryClient();
  return {
    queryClient,
    ...render(ui, {
      wrapper: createWrapper({ ...options, queryClient }),
      ...options,
    }),
  };
}
