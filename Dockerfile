FROM node:26-alpine AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

FROM eclipse-temurin:25-jdk-alpine AS build
ARG GITHUB_ACTIONS=false
ARG GITHUB_REF_TYPE=null
ARG GITHUB_REF_NAME=null
ARG GITHUB_SHA=null
WORKDIR /build
COPY . .
RUN ./gradlew --no-daemon shadowJar -x test

FROM eclipse-temurin:25-jre-alpine
ENV DOCKER=true
WORKDIR /app
COPY --from=frontend /build/dist/ public
COPY --from=build /build/build/libs/rep-bot-*-all.jar bot.jar
COPY docker/config/log4j2.xml config/
COPY assets assets
RUN echo "{}" > config/config.json
ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j2.configurationFile=config/log4j2.xml", "-Dcjda.localisation.error.name=false", "-jar", "bot.jar"]
