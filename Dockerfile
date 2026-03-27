FROM amd64/eclipse-temurin:25

ADD /build/libs/app.jar app.jar

ARG COMMIT_HASH
ENV COMMIT_HASH=$COMMIT_HASH

ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080
