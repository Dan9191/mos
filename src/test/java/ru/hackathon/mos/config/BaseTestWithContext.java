package ru.hackathon.mos.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Базовый класс с полным контекстом сервиса для тестов.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseTestWithContext {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> String.format("jdbc:postgresql://%s:%d/test?currentSchema=edu_service",
                        TestContainersFactory.POSTGRES.getHost(), TestContainersFactory.POSTGRES.getFirstMappedPort()));
        registry.add("spring.datasource.username", TestContainersFactory.POSTGRES::getUsername);
        registry.add("spring.datasource.password", TestContainersFactory.POSTGRES::getPassword);


    }

}
