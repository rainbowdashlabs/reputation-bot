# Dev image - the source is bind-mounted at /home/gradle/project, while the Gradle home
# (~/.gradle), the project's build/ and the project-local .gradle/ live in named volumes so
# incremental compilation survives container restarts. Once the cache is warm, building in
# here is as fast as building on the host.
FROM gradle:jdk21-alpine

WORKDIR /home/gradle/project

# AWT needs a font configuration to render the xchart images the ranking and profile
# commands produce; the alpine base ships neither fontconfig nor a font.
RUN apk add --no-cache fontconfig ttf-dejavu

ENV GRADLE_USER_HOME=/home/gradle/.gradle

EXPOSE 8888

# The project's wrapper, so the Gradle version matches gradle/wrapper/gradle-wrapper.properties.
# It downloads its distribution into GRADLE_USER_HOME on first run and the named volume keeps it
# there, so later starts skip the download. The run task forwards these -D flags to the forked
# bot JVM; they are the same ones the IntelliJ "App-Testing" configuration passes. Everything
# that differs per stack (database, API url, SKU grants) is an environment variable instead and
# is set in the compose file. `docker compose restart` picks up source edits incrementally.
CMD ["./gradlew", "run", "--no-daemon", "--console=plain", \
     "-Dbot.config=config/config.testing.json", \
     "-Dlog4j2.configurationFile=docker/config/log4j2.testing.xml", \
     "-Dcjda.localisation.error.name=false", \
     "-Dcjda.interactions.cleanguildcommands=true", \
     "-Dcjda.interactions.testmode=true"]
