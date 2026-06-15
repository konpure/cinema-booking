FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npm run build

FROM alpine:3.19
WORKDIR /dist
COPY --from=build /app/dist .
CMD ["sh", "-c", "cp -r /dist/* /output/ && tail -f /dev/null"]
