FROM eclipse-temurin:21

ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT java ${JVM_OPTS} -XX:+ExitOnOutOfMemoryError -jar /app.jar ${APP_ARGS}
