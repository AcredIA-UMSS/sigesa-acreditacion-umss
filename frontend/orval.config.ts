import { defineConfig } from 'orval';

export default defineConfig({
  sigesa: {
    input: {
      // Esta es la ruta por defecto donde Spring Boot expone tu OpenAPI (Swagger)
      target: 'http://localhost:8080/v3/api-docs',
    },
    output: {
      mode: 'tags-split', // Separa los archivos por tags (ej. Auth, Process)
      target: 'src/api/endpoints', // Dónde guardará los hooks de React Query
      schemas: 'src/api/model',    // Dónde guardará las interfaces de TypeScript (DTOs)
      client: 'react-query',       // Dile que genere hooks para TanStack Query
    },
  },
});