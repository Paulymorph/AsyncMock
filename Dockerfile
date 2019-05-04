FROM gafiatulin/alpine-sbt

RUN mkdir -p /app
WORKDIR /app

COPY . .

RUN apk add --no-cache curl && \
    bash ./scripts/download-swagger-ui.sh  ./src/main/resources/swagger && \
    sbt assembly

EXPOSE 2525

ENTRYPOINT ["java", "-jar", "target/scala-2.12/AsyncMock.jar"]