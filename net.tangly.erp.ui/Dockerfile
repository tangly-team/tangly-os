# Allows you to run this app easily in a docker image.
# To build the image use:
# build applicaton ./gradlew net.tangly.erp.ui:build installDist -Pvaadin.productionMode
# docker tanglyllc/tangly-erp:latest .
# To run the image use:
# docker run --rm -ti -p 8080:8080 -e PORT=8080  -v /var/tangly-erp:/var/tangly-erp tanglyllc/tangly-erp:latest

FROM eclipse-temurin:22

RUN mkdir -p /var/tangly-erp
RUN mkdir -p /var/tangly-erp/import
RUN mkdir -p /var/tangly-erp/db
RUN mkdir -p /var/tangly-erp/reports
RUN mkdir -p /app

COPY build/install/net.tangly.erp.ui/ /app/
COPY src/main/config/app-in-memory-no-imports.properties /app/

RUN groupadd erp
RUN useradd -m -g erp erp
RUN chown -R erp:erp /app
RUN chown -R erp:erp /var/tangly-erp

USER erp
WORKDIR /app/bin
EXPOSE $PORT
ENTRYPOINT ["./net.tangly.erp.ui",  "-p $PORT -m true -c ../config/app-in-memory-no-imports.properties"]
