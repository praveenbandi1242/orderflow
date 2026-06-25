package com.orderflow.config;

import org.testcontainers.containers.MySQLContainer;

public class MySqlContainerConfig {

    private static final MySQLContainer<?> MYSQL_CONTAINER =
            new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("orderflow")
                    .withUsername("root")
                    .withPassword("root")
                    .withReuse(true);

    static {

        MYSQL_CONTAINER.start();

    }

    public static MySQLContainer<?> getContainer() {

        return MYSQL_CONTAINER;

    }
}
