package com.pvs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.pvs.mapper")
@SpringBootApplication
public class PvsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PvsApplication.class, args);
    }

}
