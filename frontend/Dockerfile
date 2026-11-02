# frontend/Dockerfile
# Etapa 1: Construcción. Node, pnpm y las devDependencies no pasan a la imagen final.
FROM node:22-alpine@sha256:fe4f6d74e20370acb04ad889077d14d0f64a9c8ea6a42bc60cddf1e844699914 AS builder
WORKDIR /app

ENV CI=true

RUN corepack enable && corepack prepare pnpm@11.18.0 --activate

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
RUN pnpm install --frozen-lockfile --ignore-scripts

COPY . .
ARG VITE_API_URL=
ENV VITE_API_URL=$VITE_API_URL
ARG VITE_PHASES_COPILOT_DEBUG_ACTIONS=false
ENV VITE_PHASES_COPILOT_DEBUG_ACTIONS=$VITE_PHASES_COPILOT_DEBUG_ACTIONS
ARG VITE_USERS_COPILOT_DEBUG_ACTIONS=false
ENV VITE_USERS_COPILOT_DEBUG_ACTIONS=$VITE_USERS_COPILOT_DEBUG_ACTIONS
ARG VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS=false
ENV VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS=$VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS
ARG VITE_ASSISTANT_DEBUG_ACTIONS=false
ENV VITE_ASSISTANT_DEBUG_ACTIONS=$VITE_ASSISTANT_DEBUG_ACTIONS
RUN pnpm exec tsc -b && pnpm exec vite build

# Etapa 2: nginx sin root. Escucha en 8080.
FROM nginxinc/nginx-unprivileged:alpine@sha256:b5047b9b76845eac124358373c6852377c52e4b05834a47647e955dd91cb8f1e
USER 0
RUN apk add --no-cache --upgrade \
      brotli-libs=1.2.1-r0 \
      curl=8.23.0-r0 \
      libcurl=8.23.0-r0
USER 101
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
