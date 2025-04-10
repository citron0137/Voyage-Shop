package kr.hhplus.be.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * 페이크 리포지터리 설정
 * 'fake' 또는 'local' 프로필이 활성화되면 메모리 기반 페이크 구현체가 자동으로 적용됩니다.
 * 
 * 애플리케이션 실행 시 다음과 같이 프로필을 활성화하여 사용할 수 있습니다:
 * - 명령줄: java -jar app.jar --spring.profiles.active=fake
 * - 환경 변수: export SPRING_PROFILES_ACTIVE=fake
 * - application.yml 파일: spring.profiles.active: fake
 */
@Configuration
@Profile("fake", "local")
class FakeRepositoryConfig {
    // 빈이나 추가 설정이 필요하면 여기에 추가
} 