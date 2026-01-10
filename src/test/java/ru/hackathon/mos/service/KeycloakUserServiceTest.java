package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hackathon.mos.exception.UserAlreadyExistsException;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

    @Mock
    private Keycloak keycloakAdminClient;

    @Mock
    private UserService userService;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private Response response;

    @InjectMocks
    private KeycloakUserService keycloakUserService;

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String userId;

    @BeforeEach
    void setUp() {
        username = "testuser";
        email = "test@example.com";
        password = "TestPass123";
        firstName = "Иван";
        lastName = "Иванов";
        userId = "123e4567-e89b-12d3-a456-426614174000";
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setUsername(username);

        when(keycloakAdminClient.realm("hackathon")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(List.of(existingUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> keycloakUserService.registerUser(username, email, password, firstName, lastName)
        );

        assertThat(exception.getMessage(), containsString("Username already exists"));

        verify(keycloakAdminClient, times(1)).realm("hackathon");
        verify(usersResource, times(1)).searchByUsername(username, true);
        verify(usersResource, never()).searchByEmail(any(), any());
        verify(usersResource, never()).create(any());
        verify(userService, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setEmail(email);

        when(keycloakAdminClient.realm("hackathon")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(List.of());
        when(usersResource.searchByEmail(email, true)).thenReturn(List.of(existingUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> keycloakUserService.registerUser(username, email, password, firstName, lastName)
        );

        assertThat(exception.getMessage(), containsString("Email already registered"));

        verify(keycloakAdminClient, times(1)).realm("hackathon");
        verify(usersResource, times(1)).searchByUsername(username, true);
        verify(usersResource, times(1)).searchByEmail(email, true);
        verify(usersResource, never()).create(any());
        verify(userService, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    void registerUser_ShouldAssignDefaultRole() {
        // Arrange
        when(keycloakAdminClient.realm("hackathon")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(List.of());
        when(usersResource.searchByEmail(email, true)).thenReturn(List.of());
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://localhost:8080/admin/realms/hackathon/users/" + userId));

        org.keycloak.admin.client.resource.RoleResource roleResource = mock(org.keycloak.admin.client.resource.RoleResource.class);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("hackathon.user")).thenReturn(roleResource);

        RoleRepresentation role = new RoleRepresentation();
        when(roleResource.toRepresentation()).thenReturn(role);

        when(realmResource.users().get(userId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(mock(org.keycloak.admin.client.resource.RoleScopeResource.class));

        // Act
        keycloakUserService.registerUser(username, email, password, firstName, lastName);

        // Assert
        verify(rolesResource, times(1)).get("hackathon.user");
        verify(roleResource, times(1)).toRepresentation();
        verify(roleMappingResource.realmLevel(), times(1)).add(List.of(role));

        verify(keycloakAdminClient, times(1)).realm("hackathon");
        verify(userService, times(1)).createUser(any(), any(), any(), any(), any());
    }
}