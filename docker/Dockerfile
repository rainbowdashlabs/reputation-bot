FROM gradle:7.4-jdk18 AS build
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

FROM eclipse-temurin:18-jdk-alpine AS unzip
WORKDIR /source
COPY --from=build source/build/libs/rep-bot-*-all.jar app.jar
# Extract jar
RUN jar -xf app.jar

FROM eclipse-temurin:18-alpine AS extractDependencies
WORKDIR /extractDependencies
# Copy everything from unzip stage
COPY --from=unzip source/ ./
# Remove all unwanted files as they are copied in an own layer
RUN rm -r \
        Log4j* \
        log4j2.xml \
        META-INF/ \
        locale* \
        database/ \
        Thankswords.json \
        version \
        de/chojo/repbot \
        *.jar

FROM eclipse-temurin:18-alpine
WORKDIR /
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
CMD ["sh", "start.sh"]
