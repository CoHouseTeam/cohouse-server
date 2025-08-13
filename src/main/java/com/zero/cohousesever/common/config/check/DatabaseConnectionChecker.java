package com.zero.cohousesever.common.config.check;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseConnectionChecker implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.queryForObject("SELECT 1", String.class); // 테스트 쿼리
            System.out.println("✅ MySQL 연결 성공!");
        } catch (Exception e) {
            System.out.println("❌ MySQL 연결 실패: " + e.getMessage());
        }
    }
}
