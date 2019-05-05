FROM openjdk:jre-alpine

ENV sbt_home /usr/local/sbt
ENV PATH ${PATH}:${sbt_home}/bin

# Install sbt: find latest version, download, unpack
RUN apk add --no-cache curl && \
    mkdir -p "$sbt_home" && \
    curl -s https://api.github.com/repos/sbt/sbt/releases/latest \
     | grep "browser_download_url.*tgz\"$" \
     | cut -d '"' -f 4 \
     | xargs -n 1 curl -L \
     | tar xvz -C $sbt_home --strip-components=1

RUN mkdir -p /app
WORKDIR /app

COPY . .

# Build the application with SBT assembly
# Cleanup the unnecessary SBT garbage after build
RUN apk add --no-cache curl && \
    apk add --no-cache bash && \
    sh ./scripts/download-swagger-ui.sh  ./src/main/resources/swagger && \
    sbt assembly && \
    mv target/scala-2.12/AsyncMock.jar . && \
    find . ! -name 'AsyncMock.jar' -type f -exec rm -f {} + && \
    find . -type d -not -name 'AsyncMock.jar' -delete && \
    rm -rf ${sbt_home} && \
    rm -rf /root/.ivy2 && \
    rm -rf /root/.sbt && \
    apk del curl && \
    apk del bash

EXPOSE 2525

ENTRYPOINT ["java", "-jar", "AsyncMock.jar"]