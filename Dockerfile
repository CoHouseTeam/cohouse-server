FROM amazoncorretto:21

# Tesseract 및 의존 라이브러리 설치
RUN yum install -y epel-release && \
    yum install -y tesseract tesseract-langpack-kor \
    leptonica libtiff libpng libjpeg gomp libarchive

# 환경변수 설정
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV LD_LIBRARY_PATH=/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]