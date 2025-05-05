package kr.hhplus.be.server.integration.userpoint

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.user.UserApplication
import kr.hhplus.be.server.application.userpoint.UserPointCriteria
import kr.hhplus.be.server.application.userpoint.UserPointApplication
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
@DisplayName("사용자 포인트 흐름 통합 테스트")
class UserPointFlowIntegrationTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userPointService: UserPointService

    @Autowired
    private lateinit var userPointApplication: UserPointApplication

    @Autowired
    private lateinit var userApplication: UserApplication

    @Test
    @DisplayName("사용자 생성 후 포인트를 충전하고 조회할 수 있다")
    fun `사용자 생성 후 포인트를 충전하고 조회할 수 있다`() {
        // given: 사용자 생성
        val user = userApplication.createUser()
        
        // when: 포인트 충전
        val chargeAmount = 1000L
        val chargeCriteria = UserPointCriteria.Charge(user.userId, chargeAmount)
        val chargeResult = userPointApplication.chargePoint(chargeCriteria)
        
        // then: 충전 결과 검증
        assertThat(chargeResult.userId).isEqualTo(user.userId)
        assertThat(chargeResult.amount).isEqualTo(chargeAmount)
        
        // when: 포인트 조회
        val getCriteria = UserPointCriteria.GetByUserId(user.userId)
        val userPoint = userPointApplication.getUserPoint(getCriteria)
        
        // then: 조회 결과 검증
        assertThat(userPoint.userId).isEqualTo(user.userId)
        assertThat(userPoint.amount).isEqualTo(chargeAmount)
    }
    
    @Test
    @DisplayName("여러 번 포인트를 충전하면 합산된 금액이 조회된다")
    fun `여러 번 포인트를 충전하면 합산된 금액이 조회된다`() {
        // given: 사용자 생성
        val user = userApplication.createUser()
        
        // when: 첫번째 포인트 충전
        val firstChargeAmount = 500L
        val firstChargeCriteria = UserPointCriteria.Charge(user.userId, firstChargeAmount)
        userPointApplication.chargePoint(firstChargeCriteria)
        
        // when: 두번째 포인트 충전
        val secondChargeAmount = 700L
        val secondChargeCriteria = UserPointCriteria.Charge(user.userId, secondChargeAmount)
        userPointApplication.chargePoint(secondChargeCriteria)
        
        // when: 포인트 조회
        val getCriteria = UserPointCriteria.GetByUserId(user.userId)
        val userPoint = userPointApplication.getUserPoint(getCriteria)
        
        // then: 합산된 금액 검증
        val expectedTotalAmount = firstChargeAmount + secondChargeAmount
        assertThat(userPoint.amount).isEqualTo(expectedTotalAmount)
    }
    
    @Test
    @DisplayName("동일한 사용자의 포인트 충전은 애플리케이션 레이어에서 성공적으로 처리된다")
    fun `동일한 사용자의 포인트 충전은 애플리케이션 레이어에서 성공적으로 처리된다`() {
        // given: 사용자 생성
        val user = userApplication.createUser()
        
        // when: 포인트 충전 - UserPointFacade를 통해 (애플리케이션 레이어)
        val appLayerChargeAmount = 1500L
        val appLayerChargeCriteria = UserPointCriteria.Charge(user.userId, appLayerChargeAmount)
        val appLayerChargeResult = userPointApplication.chargePoint(appLayerChargeCriteria)
        
        // then: 충전 결과 검증
        assertThat(appLayerChargeResult.userId).isEqualTo(user.userId)
        assertThat(appLayerChargeResult.amount).isEqualTo(appLayerChargeAmount)
        
        // when: 포인트 조회
        val getCriteria = UserPointCriteria.GetByUserId(user.userId)
        val userPoint = userPointApplication.getUserPoint(getCriteria)
        
        // then: 조회 결과 검증
        assertThat(userPoint.amount).isEqualTo(appLayerChargeAmount)
    }
} 