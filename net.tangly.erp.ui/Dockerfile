# Allows you to run this app easily in a docker image.
# See https://github.com/mvysny/vaadin14-embedded-jetty-gradle/README.md for more details.
# To build the image use:
# DOCKER_BUILDKIT=0 docker build --no-cache -t tangly-erp:latest .
# To run the image use:
# docker run --rm -t -i -e JAVA_OPTS='--enable-preview'-p8080:8080 tangly-erp:latest
# an alternative docker image would be FROM openjdk:16-jdk-oracle
# TODO: Currently you must edit the script file to add the enable-preview option in the last statement calling java before the @ parameter

# FROM openjdk:16-jdk-slim
FROM openjdk:16
COPY build/distributions/net.tangly.erp.ui-0.2.6/ /app/
WORKDIR /app/bin
EXPOSE 8080
ENV JAVA_OPTS="--enable-preview"
ENTRYPOINT ./net.tangly.erp.ui

# FROM openjdk:16-jdk-alpine
# RUN apk update & apk upgrade && apk add bash
# ADD run.sh runs.sh
# RUN chmod +x run.sh
# CMD or ENTRYPOINT ['bash', './net.tangly.erp.ui']
