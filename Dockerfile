FROM gafiatulin/alpine-sbt

RUN mkdir -p /app
WORKDIR /app

COPY . .

RUN apk add --no-cache curl
RUN bash ./scripts/download-swagger-ui.sh  ./src/main/resources/swagger
RUN apk del curl

RUN sbt compile

EXPOSE 2525

ENTRYPOINT ["sbt", "run"]