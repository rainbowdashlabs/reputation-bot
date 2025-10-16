FROM gradle:jdk21-alpine as build

ARG GITHUB_ACTIONS=false
ARG GITHUB_REF_TYPE=null
ARG GITHUB_REF_NAME=null
ARG GITHUB_SHA=null
ARG GITHUB_ACTIONS=$GITHUB_ACTIONS
ARG GITHUB_REF_TYPE=$GITHUB_REF_TYPE
ARG GITHUB_REF_NAME=$GITHUB_REF_NAME
ARG GITHUB_SHA=$GITHUB_SHA

COPY . .
RUN gradle clean shadowJar --no-daemon -x test -x javadocJar -x sourcesJar

FROM eclipse-temurin:21-alpine as runtime
ENV DOCKER=true

WORKDIR /app

COPY --from=build /home/gradle/build/libs/rep-bot-*-all.jar bot.jar

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j2.configurationFile=config/log4j2.xml", "-Dcjda.localisation.error.name=false", "-jar" , "bot.jar"]
