FROM gradle:7.1-jdk16 AS build
WORKDIR /source
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY src/ src/
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:15-jdk-alpine AS unzip
WORKDIR /source
COPY --from=build source/build/libs/rep-bot-*-all.jar app.jar
RUN jar -xf app.jar
RUN ls

FROM openjdk:15-alpine AS extractDependencies
WORKDIR /extractDependencies
COPY --from=unzip source/ ./
RUN rm Log4j*
RUN rm log4j2.xml
RUN rm META-INF/ -r
RUN rm locale*
RUN rm database/ -r
RUN rm Thankswords.json
RUN rm version
RUN rm de/chojo/repbot -r
RUN ls

FROM openjdk:15-alpine
WORKDIR /application
ENV TERM xterm-256color
COPY --from=unzip source/Log4j* ./
COPY --from=unzip source/log4j2.xml config/log4j2.xml
COPY --from=extractDependencies extractDependencies/ ./
COPY --from=unzip source/META-INF/ META-INF/
COPY --from=unzip source/locale* ./
COPY --from=unzip source/database database
COPY --from=unzip source/Thankswords.json Thankswords.json
COPY --from=unzip source/version version
COPY --from=unzip source/de/chojo/repbot de/chojo/repbot
RUN ls
ADD docker/start.sh start.sh
RUN chmod +x start.sh
ENTRYPOINT ["./start.sh"]