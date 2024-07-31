# Allows you to run this app easily in a docker image.
# To build the image use:
# build applicaton ./gradlew net.tangly.erp.ui:build installDist -Pvaadin.productionMode
# docker tanglyllc/tangly-erp:latest .
# To run the image use:
# docker run --rm -ti -p 8080:8080 -e PORT=8080  -v /var/tangly-erp:/var/tangly-erp tanglyllc/tangly-erp:latest

FROM eclipse-temurin:22

RUN apt-get update && apt-get install findutils

RUN mkdir -p /var/tangly-erp
RUN mkdir -p /app

COPY build/install/net.tangly.erp.ui/ /app/

RUN groupadd erp
RUN useradd -m -g erp erp
RUN chown -R erp:erp /app
RUN chown -R erp:erp /var/tangly-erp

USER erp
WORKDIR /app/bin
EXPOSE $PORT
ENTRYPOINT ["./net.tangly.erp.ui"]
