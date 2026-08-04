import { defineConfig } from 'orval';

export default defineConfig({
  sigesa: {
    input: {
      target: 'http://localhost:8080/v3/api-docs',
    },
    output: {
      mode: 'tags-split',
      target: 'src/api/endpoints',
      schemas: 'src/api/model',
      client: 'react-query',
      tsconfig: './tsconfig.app.json',
      override: {
        mutator: {
          path: 'src/lib/api/customFetch.ts',
          name: 'customFetch',
        },
      },
    },
  },
});
