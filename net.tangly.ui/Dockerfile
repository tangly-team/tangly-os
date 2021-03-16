# Build stage
FROM openjdk:15 AS BUILD
COPY build/distributions/net.tangly.ui-0.2.6.zip /app/
WORKDIR /app
RUN unzip net.tangly.ui-0.2.6.zip

# Run stage
FROM openjdk:15
COPY --from=BUILD /app/net.tangly.ui /app/
WORKDIR /app/bin
EXPOSE 8080
ENTRYPOINT ./net.tangly.ui
