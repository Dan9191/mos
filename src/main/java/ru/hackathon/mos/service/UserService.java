package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.entity.UserType;
import ru.hackathon.mos.repository.UserRepository;
import ru.hackathon.mos.repository.UserTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    public User findOrCreateFromJwt(Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        String role = extractRole(jwt);
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        UserType userType = userTypeRepository.findByName(role)
                .orElseGet(() -> {
                    UserType ut = new UserType();
                    ut.setName(role);
                    userTypeRepository.save(ut);
                    log.info("A user type with role '{}' was created.", role);
                    return ut;
                });

        User user = userRepository.findById(userId)
                .orElseGet(() -> {
                    User u = new User();
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

        // todo 3. Если данные поменялись → обновить
//        boolean changed = false;
//        if (!user.getFirstName().equals(firstName)) {
//            user.setFirstName(firstName);
//            changed = true;
//        }
//        if (!user.getLastName().equals(lastName)) {
//            user.setLastName(lastName);
//            changed = true;
//        }
//        if (!user.getType().equals(userType)) {
//            user.setType(userType);
//            changed = true;
//        }
//
//        if (changed) {
//            user = userRepository.save(user);
//        }

        return user;
    }

    /**
     * Определяем роль пользователя.
     *
     * @param jwt Jwt токен.
     * @return роль.
     */
    private String extractRole(Jwt jwt) {
        List<String> roles = jwt.getClaim("roles");

        if (roles.contains("ROLE_hackathon.admin")) return "Администратор";
        if (roles.contains("ROLE_hackathon.manager")) return "Менеджер";
        return "Пользователь";
    }
}
