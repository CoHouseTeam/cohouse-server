FROM ubuntu:23.04

RUN apt-get update && apt-get install -y software-properties-common && \
    add-apt-repository ppa:alex-p/tesseract-ocr-devel -y && \
    apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk \
    tesseract-ocr tesseract-ocr-kor libtesseract-dev libleptonica-dev \
    libpng16-16 libjpeg-turbo8 libtiff-dev libgomp1 libarchive13 wget curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY temp /app/temp

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
