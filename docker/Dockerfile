FROM openjdk:8-jre-slim

WORKDIR "/nanodefiner"

RUN apt-get update && apt-get install -y wget unzip && \
    apt-get purge && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/NanoDefiner/NanoDefiner/releases/download/v2.0.0/nanodefiner_2.0.0.zip && unzip nanodefiner_2.0.0.zip && rm nanodefiner_2.0.0.zip

COPY config /nanodefiner/config/

ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "/nanodefiner/NanoDefiner.war"]
