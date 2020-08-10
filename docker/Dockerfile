FROM openjdk:8-jre-slim

WORKDIR "/nanodefiner"

RUN apt-get update && apt-get install -y wget unzip && \
    apt-get purge && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/NanoDefiner/NanoDefiner/releases/download/v1.0.3/nanodefiner_1.0.3.zip && unzip nanodefiner_1.0.3.zip && rm nanodefiner_1.0.3.zip

COPY config /nanodefiner/config/

ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "/nanodefiner/NanoDefiner.war"]
