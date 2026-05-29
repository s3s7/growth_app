#FROM openjdk:11-slim

#RUN apt-get update
#WORKDIR /app
FROM openjdk:17
COPY ./out/production/DockerHelloWorld/ /tmp
WORKDIR /tmp
ENTRYPOINT ["java","HelloWorld"]