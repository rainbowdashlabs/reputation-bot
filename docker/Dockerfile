FROM openjdk:15-jdk-alpine
COPY . .
RUN ./gradlew shadowJar


FROM openjdk:15-jdk-alpine
COPY --from=0 ./build/libs/rep-bot-*-all.jar app.jar
ENV TERM xterm-256color
ADD docker/start.sh start.sh

RUN chmod +x start.sh

ENTRYPOINT ["./start.sh"]