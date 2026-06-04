package com.example.gym;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FitLife 健身房管理系统 Spring Boot 启动类。
 * @SpringBootApplication 启用自动配置，@MapperScan 扫描 com.example.gym.mapper 包下的所有 MyBatis Mapper 接口。
 * <p>
 * 运行依赖：MySQL 8.0 + Redis 6.0+ + JDK 17 +
 * 后端默认端口 8080，前端默认端口 5173。
 */
@SpringBootApplication
@MapperScan("com.example.gym.mapper")
public class GymSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymSystemApplication.class, args);
    }

}
