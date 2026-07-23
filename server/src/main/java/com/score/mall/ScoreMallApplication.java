package com.score.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.score.mall.mapper")
@EnableTransactionManagement
public class ScoreMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScoreMallApplication.class, args);
    }
}
