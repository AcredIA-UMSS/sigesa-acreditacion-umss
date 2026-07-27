# frontend/Dockerfile
# Etapa 1: Construcción
FROM node:22-alpine AS builder
WORKDIR /app

COPY package.json ./
RUN npm install

COPY . .

# 👇 Reemplazamos "RUN npm run build" por npx 👇
RUN npx tsc -b && npx vite build

# Etapa 2: Servidor Web
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]