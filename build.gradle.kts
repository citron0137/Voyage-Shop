plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("kapt") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.jpa") version "2.1.0"
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmToolchain(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

    // DB
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
	
	// OpenTelemetry
	implementation("io.opentelemetry:opentelemetry-api:1.36.0")
	implementation("io.opentelemetry:opentelemetry-sdk:1.36.0")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.36.0")
	implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.3.0")
	implementation("io.micrometer:micrometer-registry-prometheus:1.12.4")
	implementation("io.opentelemetry.javaagent:opentelemetry-javaagent:2.3.0")

	// Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
	testImplementation("org.assertj:assertj-core:3.25.3")
	testImplementation("io.mockk:mockk:1.9.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}

// OpenTelemetry 에이전트 다운로드 태스크
tasks.register<DefaultTask>("downloadOpenTelemetryAgent") {
    doLast {
        val agentVersion = "2.3.0"
        val agentFile = project.file("opentelemetry-javaagent.jar")
        if (!agentFile.exists()) {
            val url = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${agentVersion}/opentelemetry-javaagent.jar"
            println("OpenTelemetry 에이전트 다운로드 중: $url")
            
            val connection = java.net.URL(url).openConnection()
            connection.connect()
            connection.getInputStream().use { input ->
                java.io.FileOutputStream(agentFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            println("OpenTelemetry 에이전트가 다운로드되었습니다: ${agentFile.absolutePath}")
        } else {
            println("OpenTelemetry 에이전트가 이미 존재합니다: ${agentFile.absolutePath}")
        }
    }
}

// 부트런 작업 시 에이전트 자동 다운로드
tasks.named("bootRun") {
    dependsOn("downloadOpenTelemetryAgent")
}
