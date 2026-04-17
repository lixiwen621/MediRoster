package com.mediroster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MediRoster 后端入口。
 *
 * @author tongguo.li
 */
@SpringBootApplication
@MapperScan("com.mediroster.mapper")
public class MediRosterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediRosterApplication.class, args);
    }
}
