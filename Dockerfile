# Dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app

# JAR 파일만 복사
COPY build/libs/*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]