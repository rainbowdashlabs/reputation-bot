FROM eclipse-temurin:19-alpine as build

COPY . .
RUN ./gradlew clean build --no-daemon

FROM eclipse-temurin:19-alpine as runtime

WORKDIR /app

COPY --from=build /build/libs/rep-bot-*-all.jar bot.jar

ENTRYPOINT ["java", "-Dbot.config=config/config.json", "-Dlog4j.configurationFile=config/log4j2.xml", "-jar" , "bot.jar"]
