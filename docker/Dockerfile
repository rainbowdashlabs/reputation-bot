FROM gradle:jdk21-alpine as build

COPY . .
RUN gradle clean shadowJar --no-daemon -x test -x javadocJar -x sourcesJar

FROM eclipse-temurin:21-alpine as runtime
ENV DOCKER=true

WORKDIR /app

COPY --from=build /home/gradle/build/libs/rep-bot-*-all.jar bot.jar

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j2.configurationFile=config/log4j2.xml", "-Dlog4j.debug=true", "-Dcjda.localisation.error.name=false", "-jar" , "bot.jar"]
