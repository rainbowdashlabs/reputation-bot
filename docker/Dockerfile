FROM gradle:jdk21-alpine as build

COPY . .
RUN gradle clean build --no-daemon -x test

FROM eclipse-temurin:21-alpine as runtime

WORKDIR /app

COPY --from=build /home/gradle/build/libs/rep-bot-*-all.jar bot.jar

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j.configurationFile=config/log4j2.xml", "-Dcjda.localisation.error.name=false", "-jar" , "bot.jar"]
