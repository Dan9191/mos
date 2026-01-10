package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import ru.hackathon.mos.dto.UserTypeEnum;
import ru.hackathon.mos.dto.user.UpdateUserRequest;
import ru.hackathon.mos.dto.user.UserViewDto;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.entity.UserType;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.repository.UserRepository;
import ru.hackathon.mos.repository.UserTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTypeRepository userTypeRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User testUser;
    private UserType userType;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testDateTime = LocalDateTime.of(2024, 12, 10, 10, 30, 0);

        userType = UserType.builder()
                .id(1)
                .name("USER")
                .description("Обычный пользователь")
                .build();

        testUser = User.builder()
                .id(userId)
                .type(userType)
                .username("ivanov")
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .email("ivan@example.com")
                .createdAt(testDateTime)
                .build();
    }

    @Test
    void findOrCreateFromJwt_ShouldReturnExistingUser() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("preferred_username")).thenReturn("ivanov");
        when(jwt.getClaimAsString("email")).thenReturn("ivan@example.com");
        when(jwt.getClaimAsString("given_name")).thenReturn("Иван");
        when(jwt.getClaimAsString("family_name")).thenReturn("Иванов");
        when(jwt.getClaim("roles")).thenReturn(List.of("hackathon.user"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userTypeRepository.findById(UserTypeEnum.USER.getId()))
                .thenReturn(Optional.of(userType));

        // Act
        User result = userService.findOrCreateFromJwt(jwt);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(userId));
        assertThat(result.getUsername(), is("ivanov"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateFromJwt_ShouldCreateNewUser_WhenUserNotFound() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaim("preferred_username")).thenReturn("newuser");
        when(jwt.getClaimAsString("email")).thenReturn("new@example.com");
        when(jwt.getClaimAsString("given_name")).thenReturn("Новый");
        when(jwt.getClaimAsString("family_name")).thenReturn("Пользователь");
        when(jwt.getClaim("roles")).thenReturn(List.of("hackathon.user"));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userTypeRepository.findById(UserTypeEnum.USER.getId()))
                .thenReturn(Optional.of(userType));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.findOrCreateFromJwt(jwt);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(userId));
        assertThat(result.getUsername(), is("newuser"));
        assertThat(result.getEmail(), is("new@example.com"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findUserById_ShouldReturnUserDto() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserViewDto result = userService.findUserById(userId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(userId));
        assertThat(result.getUsername(), is("ivanov"));
        assertThat(result.getFirstName(), is("Иван"));
        assertThat(result.getLastName(), is("Иванов"));
        assertThat(result.getSurname(), is("Иванович"));
        assertThat(result.getEmail(), is("ivan@example.com"));
        assertThat(result.getType(), is(notNullValue()));
        assertThat(result.getType().getId(), is(1));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findUserById(userId)
        );

        assertThat(exception.getMessage(), containsString("user '" + userId + "' not exists"));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void update_ShouldUpdateUserSuccessfully() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("НовоеИмя");
        request.setLastName("НоваяФамилия");
        request.setSurname("НовоеОтчество");
        request.setEmail("newemail@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserViewDto result = userService.update(userId, request);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(userId));
        assertThat(result.getFirstName(), is("НовоеИмя"));
        assertThat(result.getLastName(), is("НоваяФамилия"));
        assertThat(result.getSurname(), is("НовоеОтчество"));
        assertThat(result.getEmail(), is("newemail@example.com"));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void findAllUsers_ShouldReturnPageOfUsers() {
        // Arrange
        User user2 = User.builder()
                .id(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"))
                .type(userType)
                .username("petrov")
                .firstName("Петр")
                .lastName("Петров")
                .email("petr@example.com")
                .createdAt(testDateTime)
                .build();

        Page<User> userPage = new PageImpl<>(
                List.of(testUser, user2),
                PageRequest.of(0, 10),
                2L
        );

        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        // Act
        Page<UserViewDto> result = userService.findAllUsers(PageRequest.of(0, 10));

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getContent(), hasSize(2));
        assertThat(result.getTotalElements(), is(2L));

        UserViewDto firstUser = result.getContent().get(0);
        assertThat(firstUser.getId(), is(userId));
        assertThat(firstUser.getUsername(), is("ivanov"));

        UserViewDto secondUser = result.getContent().get(1);
        assertThat(secondUser.getUsername(), is("petrov"));
        verify(userRepository, times(1)).findAll(any(PageRequest.class));
    }
}
