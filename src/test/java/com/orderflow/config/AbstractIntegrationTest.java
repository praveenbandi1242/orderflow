package com.orderflow.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                () -> MySqlContainerConfig.getContainer().getJdbcUrl());

        registry.add(
                "spring.datasource.username",
                () -> MySqlContainerConfig.getContainer().getUsername());

        registry.add(
                "spring.datasource.password",
                () -> MySqlContainerConfig.getContainer().getPassword());
    }
}