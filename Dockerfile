FROM gradle:8.6.0-jdk17 AS build

# 빌드 시 Git 버전을 받을 수 있도록 ARG 정의
ARG VERSION=1.0.0

WORKDIR /app
COPY . .
# 전달받은 버전 사용
RUN sed -i "s/version = getGitHash()/version = \"${VERSION}\"/g" build.gradle.kts
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 