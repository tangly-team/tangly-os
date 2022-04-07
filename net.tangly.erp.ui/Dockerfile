# Allows you to run this app easily in a docker image.
# See https://github.com/mvysny/vaadin14-embedded-jetty-gradle/README.md for more details.
# To build the image use:
# unzip the distribution file
# build applicaton docker ./gradle build -Pvaadin.productionMode
# docker build --no-cache -t tangly-erp:latest .
# To run the image use:
# docker run --rm -ti -p8080:9090 tangly-erp:latest
# an alternative docker image would be FROM openjdk:16-jdk-oracle

FROM openjdk:18
COPY build/distributions/net.tangly.erp.ui-0.2.7/ /app/
WORKDIR /app/bin
RUN useradd -m erp
USER erp
EXPOSE 8080
ENTRYPOINT ./net.tangly.erp.ui
