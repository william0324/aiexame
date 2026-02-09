package org.example.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.example.mapper") // 假设Mapper接口在org.example.mapper包下
public class MybatisPlusConfiguration {
}
