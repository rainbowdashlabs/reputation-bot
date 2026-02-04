FROM nixos/nix:latest AS frontend

WORKDIR /build/
RUN nix-channel --update
COPY shell.nix .

COPY frontend/package*.json /build
RUN nix-shell --run "npm ci"
COPY frontend/ .
RUN nix-shell --run "npm run build"

FROM gradle:jdk21-alpine AS build

ARG GITHUB_ACTIONS=false
ARG GITHUB_REF_TYPE=null
ARG GITHUB_REF_NAME=null
ARG GITHUB_SHA=null
ARG GITHUB_ACTIONS=$GITHUB_ACTIONS
ARG GITHUB_REF_TYPE=$GITHUB_REF_TYPE
ARG GITHUB_REF_NAME=$GITHUB_REF_NAME
ARG GITHUB_SHA=$GITHUB_SHA

WORKDIR /home/gradle

COPY . .

RUN mkdir -p src/main/resources/static
RUN gradle clean shadowJar --no-daemon -x test -x javadocJar -x sourcesJar

FROM eclipse-temurin:25-alpine AS runtime
ENV DOCKER=true

WORKDIR /app

COPY --from=frontend /build/dist/ public
COPY --from=build /home/gradle/build/libs/rep-bot-*-all.jar bot.jar

RUN mkdir config
RUN echo "{}" > config/config.json
COPY docker/config/log4j2.xml config/

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j2.configurationFile=config/log4j2.xml", "-Dcjda.localisation.error.name=false", "-jar" , "bot.jar"]
