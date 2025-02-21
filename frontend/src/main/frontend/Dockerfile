FROM node:20-alpine3.20 AS build
COPY package.json package-lock.json .
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:stable-alpine AS production
COPY --from=build nginx /etc/nginx/conf.d
COPY --from=build out  /usr/share/nginx/html
ENTRYPOINT ["nginx", "-g", "daemon off;"]

