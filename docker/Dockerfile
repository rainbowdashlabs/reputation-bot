FROM gradle:7.1-jdk16 AS build
WORKDIR /source
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY src/ src/
RUN ./gradlew shadowJar

FROM openjdk:15-jdk-alpine AS unzip
WORKDIR /source
COPY --from=build source/build/libs/rep-bot-*-all.jar app.jar
RUN jar -xf app.jar

FROM openjdk:15-alpine
WORKDIR /application
ENV TERM xterm-256color
COPY --from=unzip source/Log4j* ./
COPY --from=unzip source/log4j2.xml config/log4j2.xml
COPY --from=unzip source/dependencies/ dependencies/
COPY --from=unzip source/javax/ javax/
COPY --from=unzip source/natives/ natives/
COPY --from=unzip source/META-INF/ META-INF/
COPY --from=unzip source/locale* ./
COPY --from=unzip source/database database
COPY --from=unzip source/Thankswords.json Thankswords.json
COPY --from=unzip source/version version
COPY --from=unzip source/de de
ADD docker/start.sh start.sh
RUN chmod +x start.sh
ENTRYPOINT ["./start.sh"]