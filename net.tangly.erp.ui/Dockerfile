# Allows you to run this app easily in a docker image.
# See https://github.com/mvysny/vaadin14-embedded-jetty-gradle/README.md for more details.
# To build the image use:
# build applicaton docker gradle build installDist -Pvaadin.productionMode
# docker tanglyllc/tangly-erp:latest .
# To run the image use:
# docker run --rm -ti -p 8080:8080 -e PORT=8080  -v /var/tangly-erp:/var/tangly-erp tanglyllc/tangly-erp:latest
# chmod o+rwx
FROM openjdk:18
RUN microdnf install findutils

RUN mkdir -p /var/tangly-erp
RUN mkdir -p /var/tangly-erp/import
RUN mkdir -p /var/tangly-erp/db
RUN mkdir -p /var/tangly-erp/reports

COPY build/install/net.tangly.erp.ui/ /app/
WORKDIR /app/bin
RUN useradd -m erp
USER erp
EXPOSE $PORT
ENTRYPOINT ["./net.tangly.erp.ui",  "-p $PORT -m true"]
