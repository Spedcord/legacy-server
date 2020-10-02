FROM maven:3-openjdk-11 AS build

COPY ./pom.xml ./pom.xml
COPY ./src ./src

RUN mvn dependency:go-offline -B
RUN mvn package


FROM openjdk:11-jre-slim

WORKDIR /server
COPY --from=build target/server-*.jar ./server.jar

CMD ["java", "-jar", "./server.jar"]
