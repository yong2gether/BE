# Dockerfile
FROM --platform=linux/amd64 openjdk:17-jdk-slim
WORKDIR /app

# JAR 파일만 복사
COPY build/libs/ywave-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]