# Backend for msi.mos.ru users

## Описание
Сервис, которые обеспечивают полный цикл заказа и строительства объектов индивидуального жилищного строительства

## Swagger
Доступен по

https://api.mos-hack.ru/api/swagger-ui/index.html

## Сборка

Требуется Java 21 и Gradle.

### Сборка
```shell
./gradlew clean build
```

### Запуск
```shell
./gradlew bootRun
```

## Конфигурация

Настройки в `src/main/resources/application.yaml`:


| Переменная                                | Значение по умолчанию                                   | Описание                                    |
|-------------------------------------------|---------------------------------------------------------|---------------------------------------------|
| SERVER_PORT                               | 8080                                                    | Порт сервиса                                |
| SPRING_APPLICATION_NAME                   | eduinstitution                                          | Имя приложения                              |
| SPRING_DATASOURCE_URL                     | jdbc:postgresql://localhost:5432/test?currentSchema=mos | URL базы данных PostgreSQL                  |
| DATASOURCE_NAME                           | test                                                    | Пользователь базы данных                    |
| DATASOURCE_PASSWORD                       | test                                                    | Пароль базы данных                          |
| KEYCLOAK                                  | http://localhost:9090/realms/hackathon                  | Ссылка на сервис аутентификации             |
| UPLOAD_BASE_URL                           | http://localhost:8091/api/files                         | Шаблон для формирования ссылки на документы |