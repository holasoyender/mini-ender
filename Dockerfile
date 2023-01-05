FROM eclipse-temurin:19-jre-focal

RUN groupadd -g 322 miniender && \
    useradd -r -u 322 -g miniender miniender

WORKDIR /opt/miniender

RUN chown -R miniender:miniender /opt/miniender

USER miniender

COPY build/libs/miniender-*-PRIVATE_ALPHA.jar miniender.jar

EXPOSE 6690
ENTRYPOINT ["java", "-Xmx4G", "-jar", "miniender.jar"]

