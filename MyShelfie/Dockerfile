FROM openjdk:latest

# Download java 19 sdk
RUN curl -O https://download.oracle.com/java/19/archive/jdk-19.0.2_linux-x64_bin.tar.gz \
    && tar -xzf jdk-19.0.2_linux-x64_bin.tar.gz -C /usr/local \
    && rm jdk-19.0.2_linux-x64_bin.tar.gz

# Set Java home environment variable
ENV JAVA_HOME=/usr/local/jdk-19.0.2

# Set PATH to include Java binaries
ENV PATH=${JAVA_HOME}/bin:${PATH}

EXPOSE 59090 1099

WORKDIR /app

COPY target/PSP54-1.0-SNAPSHOT.jar /app/PSP54-1.0-SNAPSHOT.jar

CMD ["java", "-jar", "/app/PSP54-1.0-SNAPSHOT.jar"]
