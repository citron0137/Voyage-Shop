package kr.hhplus.be.server.controller

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
@RequestMapping("/api")
class ExampleController(private val tracer: Tracer) {

    private val logger = Logger.getLogger(ExampleController::class.java.name)

    @GetMapping("/hello")
    fun hello(): String {
        logger.info("접속 로그 - Hello API 호출됨")

        // 현재 스팬에 속성 추가
        val currentSpan = Span.current()
        currentSpan.setAttribute("custom.attribute", "hello-world")

        // 수동으로 새 스팬 생성
        val span = tracer.spanBuilder("hello-operation").startSpan()
        try {
            span.setAttribute("operation.type", "example")
            // 스팬 내부에서 작업 수행
            processHello()
            return "Hello, World!"
        } finally {
            span.end()
        }
    }

    @WithSpan // 자동으로 스팬 생성하는 어노테이션
    private fun processHello() {
        // 몇 가지 작업을 시뮬레이션
        try {
            Thread.sleep(100)
            logger.info("processHello 메소드 실행 중")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
} 