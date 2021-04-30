FROM openjdk:15-jdk-alpine
COPY . .
RUN apk update \
    && apk add git
RUN git clone https://github.com/RainbowDashLabs/cjda-util.git \
    && cd cdja-util \
    && ./gradlew publishToMavenLocal \
    && cd ..
RUN ./gradlew shadowJar


FROM openjdk:15-jdk-alpine
COPY --from=0 ./build/libs/rep-bot-*-all.jar app.jar
# COPY --from=0 ./src/main/resources/log4j2.xml log4j2.xml
ENV TERM xterm-256color
ADD docker/start.sh start.sh

RUN chmod +x start.sh

ENTRYPOINT ["./start.sh"]