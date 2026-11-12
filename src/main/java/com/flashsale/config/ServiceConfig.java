package com.flashsale.config;

import com.flashsale.service.Sleeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public Sleeper sleeper() {
        return Thread::sleep;
    }
}
