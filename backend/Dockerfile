# Java 17과 Gradle Wrapper로 실행 파일 생성
FROM eclipse-temurin:17-jdk-alpine AS builder

# 컨테이너 내부 작업 폴더 설정
WORKDIR /app

# 백엔드 소스 전체 복사
COPY . .

# Linux 실행 권한 부여 후 Spring Boot 실행 파일 생성
RUN chmod +x gradlew \
    && ./gradlew clean bootJar --no-daemon

# 가벼운 Java 17 실행 환경 사용
FROM eclipse-temurin:17-jre-alpine

# 애플리케이션 실행 폴더 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 로컬 기본 포트 문서화
EXPOSE 8080

# Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
