package com.example.gym;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.gym.mapper")
public class GymSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymSystemApplication.class, args);
    }

}
