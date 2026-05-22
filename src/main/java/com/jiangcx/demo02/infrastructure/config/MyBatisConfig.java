package com.jiangcx.demo02.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.jiangcx.demo02.infrastructure.mapper")
public class MyBatisConfig {
}
