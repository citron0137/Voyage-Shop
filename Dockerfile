FROM gradle:8.6.0-jdk17 AS build

# 빌드 시 Git 버전을 받을 수 있도록 ARG 정의
ARG VERSION=1.0.0

WORKDIR /app

# gradle 파일이 변경되었을 때만 의존 패키지 다운받게 하기
COPY build.gradle.kts settings.gradle.kts gradlew gradlew.bat ./
RUN sed -i "s/version = getGitHash()/version = \"${VERSION}\"/g" build.gradle.kts
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# 전달받은 버전 사용해서 어플리케이션 빌드
COPY . .
RUN sed -i "s/version = getGitHash()/version = \"${VERSION}\"/g" build.gradle.kts
RUN gradle bootJar --no-daemon -x test --parallel

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 