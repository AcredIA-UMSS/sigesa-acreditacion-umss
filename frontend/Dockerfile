# frontend/Dockerfile
# Etapa 1: Construcción
FROM node:22-alpine AS builder
WORKDIR /app

ENV CI=true

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
RUN pnpm install --frozen-lockfile --ignore-scripts

COPY . .
ARG VITE_API_URL=
ENV VITE_API_URL=$VITE_API_URL
RUN pnpm exec tsc -b && pnpm exec vite build

# Etapa 2: Servidor Web
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
