FROM gradle:7.1-jdk16 AS build
WORKDIR /source
# Copy all build relevant files
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN ./gradlew clean build --no-daemon > /dev/null 2>&1 || true
# Copy Code and run full build
COPY src/ src/
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:15-jdk-alpine AS unzip
WORKDIR /source
COPY --from=build source/build/libs/rep-bot-*-all.jar app.jar
# Extract jar
RUN jar -xf app.jar

FROM openjdk:15-alpine AS extractDependencies
WORKDIR /extractDependencies
# Copy everything from unzip stage
COPY --from=unzip source/ ./
# Remove all unwanted files as they are copied in an own layer
RUN rm Log4j*
RUN rm log4j2.xml
RUN rm META-INF/ -r
RUN rm locale*
RUN rm database/ -r
RUN rm Thankswords.json
RUN rm version
RUN rm de/chojo/repbot -r

FROM openjdk:15-alpine
WORKDIR /application
ENV TERM xterm-256color
# Copy all dependencies
COPY --from=extractDependencies extractDependencies/ ./
# Copy log4j configurations
COPY --from=unzip source/Log4j* ./
COPY --from=unzip source/log4j2.xml config/log4j2.xml
# Copy Meta Inf
COPY --from=unzip source/META-INF/ META-INF/
# Copy Resources
COPY --from=unzip source/locale* ./
COPY --from=unzip source/database database
COPY --from=unzip source/Thankswords.json Thankswords.json
COPY --from=unzip source/version version
# Copy classes of repbot
COPY --from=unzip source/de/chojo/repbot de/chojo/repbot
# Add start script
ADD docker/start.sh start.sh
RUN chmod +x start.sh
ENTRYPOINT ["./start.sh"]