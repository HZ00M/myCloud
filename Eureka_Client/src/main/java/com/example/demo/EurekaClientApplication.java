package com.example.demo;

import com.example.demo.annotation.EnableFilter;
import com.example.demo.enums.FilterEnum;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@EnableFilter({FilterEnum.LOG,FilterEnum.Auth})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }
}
