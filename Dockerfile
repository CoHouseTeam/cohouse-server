FROM amazoncorretto:21

# 환경변수 설정
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV TESSDATA_PREFIX=/usr/share/tessdata

# Tesseract OCR 설치
RUN yum update -y && \
    yum install -y amazon-linux-extras && \
    amazon-linux-extras enable epel && \
    yum install -y tesseract tesseract-devel && \
    yum clean all

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]