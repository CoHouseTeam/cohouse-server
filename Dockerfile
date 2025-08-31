# 빌드 스테이지: 우분투 기반에서 Tesseract 설치 및 빌드
FROM ubuntu:22.04 AS builder

RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev openjdk-21-jdk && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build/libs/*.jar app.jar

# 실행 스테이지: Amazon Corretto 베이스 이미지에 결과물만 복사
FROM amazoncorretto:21

ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV TESSDATA_PREFIX=/usr/share/tessdata

WORKDIR /app

COPY --from=builder /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]