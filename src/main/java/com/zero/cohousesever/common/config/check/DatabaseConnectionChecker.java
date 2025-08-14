package com.zero.cohousesever.common.config.check;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConnectionChecker implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.queryForObject("SELECT 1", String.class); // 테스트 쿼리
            log.info("✅ MySQL 연결 성공!");
        } catch (Exception e) {
            log.error("❌ MySQL 연결 실패: {}", e.getMessage(), e);
        }
    }
}
