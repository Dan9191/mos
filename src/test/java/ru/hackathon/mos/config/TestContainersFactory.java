package ru.hackathon.mos.config;

import org.testcontainers.containers.*;

/**
 * Синглтон фабрика тест контейнеров.
 */
public class TestContainersFactory {

    private static final String POSTGRES_IMAGE = "postgres:17.6-alpine3.21";

    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("test")
            .withUsername("sa")
            .withPassword("sa");

    static {
        POSTGRES.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            POSTGRES.stop();
        }));
    }

    private TestContainersFactory() {
    }
}
