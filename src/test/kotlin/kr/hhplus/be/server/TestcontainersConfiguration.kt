package kr.hhplus.be.server

import jakarta.annotation.PreDestroy
import org.flywaydb.core.Flyway
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

@Configuration
class TestcontainersConfiguration {
    @PreDestroy
    fun preDestroy() {
        if (mySqlContainer.isRunning) mySqlContainer.stop()
        if (redisContainer.isRunning) redisContainer.stop()
    }

    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(mySqlContainer.jdbcUrl + "?characterEncoding=UTF-8&serverTimezone=UTC")
            .username(mySqlContainer.username)
            .password(mySqlContainer.password)
            .driverClassName(mySqlContainer.driverClassName)
            .build()
    }

    @Bean
    @DependsOn("dataSource")
    fun flyway(dataSource: DataSource): Flyway {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
        
        flyway.migrate()
        return flyway
    }

    companion object {
        val mySqlContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("hhplus")
            .withUsername("test")
            .withPassword("test")
            .apply {
                start()
            }

        val redisContainer: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379)
            .apply {
                start()
            }

        init {
            System.setProperty("spring.datasource.url", mySqlContainer.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC")
            System.setProperty("spring.datasource.username", mySqlContainer.username)
            System.setProperty("spring.datasource.password", mySqlContainer.password)
            
            System.setProperty("spring.data.redis.host", redisContainer.host)
            System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString())
        }
    }
}
