package ru.hackathon.mos.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import ru.hackathon.mos.dto.UserTypeEnum;
import ru.hackathon.mos.dto.user.UpdateUserRequest;
import ru.hackathon.mos.dto.user.UserTypeViewDto;
import ru.hackathon.mos.dto.user.UserViewDto;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.entity.UserType;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.repository.UserRepository;
import ru.hackathon.mos.repository.UserTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static ru.hackathon.mos.dto.UserTypeEnum.ADMIN;
import static ru.hackathon.mos.dto.UserTypeEnum.MANAGER;
import static ru.hackathon.mos.dto.UserTypeEnum.USER;

/**
 * Сервис работы с пользователями.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий для работы с типом пользователя.
     */
    private final UserTypeRepository userTypeRepository;

    /**
     * Создание или получение пользователя.
     * Создание пользователя при первом обращении по jwt токену.
     *
     * @param jwt Jwt токен.
     * @return данные пользователя.
     */
    @Transactional
    public User findOrCreateFromJwt(Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        String login = jwt.getClaim("preferred_username");
        String email = jwt.getClaimAsString("email");
        UserTypeEnum userTypeEnum = extractRole(jwt);
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        UserType userType = userTypeRepository.findById(userTypeEnum.getId())
                .orElseThrow(() -> new NotFoundException("user_type", (long) userTypeEnum.getId()));

        return userRepository.findById(userId)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(login);
                    u.setId(userId);
                    u.setEmail(email);
                    u.setFirstName(firstName);
                    u.setLastName(lastName);
                    u.setType(userType);
                    u.setCreatedAt(LocalDateTime.now());
                    userRepository.save(u);
                    log.info("A user with ID '{}' was created.", userId);
                    return u;
                });
    }

    /**
     * Создание пользователя в БД.
     *
     * @param userId    ID пользователя.
     * @param username  Логин пользователя
     * @param email     Почта пользователя.
     * @param firstName Имя пользователя.
     * @param lastName  Фамилия пользователя.
     */
    @Transactional
    public void createUser(UUID userId, String username, String email, String firstName, String lastName) {

        UserType userType = userTypeRepository.findById(USER.getId())
                .orElseThrow(() -> new NotFoundException("user_type", (long) USER.getId()));

        User user = User.builder()
                .id(userId)
                .username(username)
                .type(userType)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        log.info("A user with ID '{}' has been created.", user.getId());
    }

    /**
     * Поиск пользователя по ID.
     *
     * @param userId ID пользователя.
     * @return найденный пользователь.
     */
    @Transactional
    public UserViewDto findUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user '%s' not exists", userId)));

        UserTypeViewDto userTypeViewDto = UserTypeViewDto.builder()
                .id(user.getType().getId())
                .name(user.getType().getName())
                .description(user.getType().getDescription())
                .build();

        log.info("A user with ID '{}' has been found.", userId);
        return UserViewDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .type(userTypeViewDto)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .surname(user.getMiddleName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Определяем роль пользователя.
     *
     * @param jwt Jwt токен.
     * @return роль.
     */
    private UserTypeEnum extractRole(Jwt jwt) {
        List<String> roles = jwt.getClaim("roles");

        if (roles.contains(ADMIN.getRole())) return ADMIN;
        if (roles.contains(MANAGER.getRole())) return MANAGER;
        return USER;
    }

    /**
     * Обновление пользователя.
     *
     * @param userId  ID пользователя.
     * @param request Запрос на обновление.
     * @return обновленная модель.
     */
    @Transactional
    public UserViewDto update(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user '%s' not exists", userId)));
        UserTypeViewDto userTypeViewDto = UserTypeViewDto.builder()
                .id(user.getType().getId())
                .name(user.getType().getName())
                .description(user.getType().getDescription())
                .build();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getSurname());
        user.setEmail(request.getEmail());
        userRepository.save(user);
        log.info("A user with ID '{}' has been updated.", userId);
        return UserViewDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .type(userTypeViewDto)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .surname(user.getMiddleName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();

    }

    /**
     * Получение пагинируемого списка всех пользователей.
     *
     * @param pageable Настройки страницы.
     * @return страница пользователей.
     */
    @Transactional
    public Page<UserViewDto> findAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        List<UserViewDto> dtos = page.getContent().stream()
                .map(user -> {
                    UserTypeViewDto userTypeViewDto = UserTypeViewDto.builder()
                            .id(user.getType().getId())
                            .name(user.getType().getName())
                            .description(user.getType().getDescription())
                            .build();
                    return UserViewDto.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .type(userTypeViewDto)
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .surname(user.getMiddleName())
                            .email(user.getEmail())
                            .createdAt(user.getCreatedAt())
                            .build();
                })
                .toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());

    }
}
