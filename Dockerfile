FROM ubuntu:22.04

# 필수 패키지 업데이터 및 환경 설정
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk \
    tesseract-ocr tesseract-ocr-kor \
    libtesseract-dev \
    libleptonica-dev \
    libpng16-16 libjpeg-turbo8 libtiff5 libgomp1 libarchive13 wget curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]