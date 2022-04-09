# Allows you to run this app easily in a docker image.
# See https://github.com/mvysny/vaadin14-embedded-jetty-gradle/README.md for more details.
# To build the image use:
# build applicaton docker gradle build installDist -Pvaadin.productionMode
# docker build --no-cache -t tangly-erp:latest .
# To run the image use:
# docker run --rm -ti -p 8080:8080 --mount type=bind,source=/Users/Shared/tangly,target=/Users/Shared/tangly tangly-erp:latest


FROM openjdk:18
RUN microdnf install findutils
COPY build/install/net.tangly.erp.ui/ /app/
WORKDIR /app/bin
RUN useradd -m erp
USER erp
EXPOSE 8080
ENTRYPOINT ./net.tangly.erp.ui
