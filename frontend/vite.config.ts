import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
  plugins: [react(), tailwindcss()],
  define: {
    'process.env.VITE_API_URL': JSON.stringify(env.VITE_API_URL ?? ''),
  },
  server: {
    port: 5173,
    proxy: {
      // Solo REST del backend. No usar '/api': colisiona con módulos Vite `/src/api/` (Orval).
      '/api/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        timeout: 300_000,
        proxyTimeout: 300_000,
        bypass(req) {
          const path = req.url ?? '';
          if (path.startsWith('/src/') || path.includes('/src/api/')) {
            return path;
          }
        },
      },
    },
  },
  preview: {
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  };
});
