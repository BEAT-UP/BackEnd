#Multi-stage build 최적화
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

#Gradle Wrapper와 build.gradle 복사(캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 의존성 다운로드(변경이 작은 부분 먼저)
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# 실행 단계
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbase --tries=1 --spider https://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM 최적화 옵션
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
