FROM openjdk:17-jdk-slim

WORKDIR /app

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 의존성 파일 복사 및 다운로드
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon

# 포트 설정
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "build/libs/fingrow-0.0.1-SNAPSHOT.jar"]