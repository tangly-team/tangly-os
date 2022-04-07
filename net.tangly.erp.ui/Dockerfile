# Allows you to run this app easily in a docker image.
# See https://github.com/mvysny/vaadin14-embedded-jetty-gradle/README.md for more details.
# To build the image use:
# build applicaton docker gradle build -Pvaadin.productionMode
# unzip the distribution file
# docker build --no-cache -t tangly-erp:latest .
# To run the image use:
# docker run --rm -ti -p 8080:8080 --mount type=bind,source=/Users/Shared/tangly,target=/Users/Shared/tangly tangly-erp:latest


FROM openjdk:18
COPY build/distributions/net.tangly.erp.ui-0.2.7/ /app/
WORKDIR /app/bin
RUN useradd -m erp
RUN microdnf install findutils
USER erp
EXPOSE 8080
ENTRYPOINT ./net.tangly.erp.ui
