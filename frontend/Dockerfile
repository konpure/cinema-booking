# ===== 构建阶段 =====
FROM node:22-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ===== 运行阶段 =====
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

# 代理后端 API 请求
RUN rm -f /etc/nginx/conf.d/default.conf
COPY <<'NGINX_EOF' /etc/nginx/conf.d/default.conf
server {
    listen       80;
    server_name  localhost;

    root   /usr/share/nginx/html;
    index  index.html;

    # 前端路由（Vue SPA）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 代理后端 API
    location /api/ {
        proxy_pass http://nginx:80/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
NGINX_EOF

EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:80/ || exit 1