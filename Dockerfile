FROM gradle:8.10.2-jdk21 AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY gradle.properties gradle.properties
COPY shared shared
COPY server server

RUN chmod +x gradlew
RUN KAZE_SERVER_ONLY=true ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/server/build/install/server /app/server

ENV PORT=8080
EXPOSE 8080

CMD ["/app/server/bin/server"]
