FROM amazoncorretto:21

# epel 활성화 후 기본 필수 라이브러리만 설치 및 캐시 제거
RUN amazon-linux-extras enable epel && \
    yum clean metadata && \
    yum install -y --setopt=tsflags=nodocs --disableexcludes=all \
        tesseract tesseract-langpack-kor \
        leptonica libtiff libpng libjpeg libgomp libarchive && \
    yum clean all && \
    rm -rf /var/cache/yum

# 환경변수 설정
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV LD_LIBRARY_PATH=/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]