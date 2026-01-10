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
import ru.hackathon.mos.dto.application.ApplicationCreateRequest;
import ru.hackathon.mos.dto.application.ApplicationDetailsDto;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.repository.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusRepository statusRepository;

    @Mock
    private ProjectTemplateRepository templateRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ApplicationService applicationService;

    private UUID userId;
    private UUID managerId;
    private Long applicationId;
    private Long templateId;
    private Application testApplication;
    private User testUser;
    private User testManager;
    private ProjectTemplate testTemplate;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        managerId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        applicationId = 1L;
        templateId = 100L;

        testTemplate = new ProjectTemplate();
        testTemplate.setId(templateId);
        testTemplate.setTitle("Проект дома");

        testUser = User.builder()
                .id(userId)
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .email("ivan@example.com")
                .build();

        testManager = User.builder()
                .id(managerId)
                .firstName("Петр")
                .lastName("Петров")
                .middleName("Петрович")
                .email("manager@example.com")
                .build();

        testApplication = new Application();
        testApplication.setId(applicationId);
        testApplication.setCreatorId(userId);
        testApplication.setProjectId(templateId);
        testApplication.setContact("+79001234567");
        testApplication.setCreatedAt(Instant.now());
    }

    @Test
    void findAllByUserSortByStatus_ShouldReturnUserApplications() {
        // Arrange
        ApplicationStatus createdStatus = mock(ApplicationStatus.class);
        when(createdStatus.getId()).thenReturn(1);

        ApplicationStatus considerationStatus = mock(ApplicationStatus.class);
        when(considerationStatus.getId()).thenReturn(2);

        testApplication.setStatus(createdStatus);

        Application application2 = new Application();
        application2.setId(2L);
        application2.setCreatorId(userId);
        application2.setProjectId(templateId);
        application2.setContact("+79001234568");
        application2.setStatus(considerationStatus);
        application2.setManagerId(managerId);
        application2.setCreatedAt(Instant.now());

        Page<Application> applicationPage = new PageImpl<>(
                List.of(testApplication, application2),
                PageRequest.of(0, 10),
                2L
        );

        when(applicationRepository.findAllByCreatorIdOrderedByStatusAndDate(userId, PageRequest.of(0, 10)))
                .thenReturn(applicationPage);
        when(userRepository.findAllById(List.of(managerId))).thenReturn(List.of(testManager));

        // Act
        Page<ApplicationDetailsDto> result = applicationService.findAllByUserSortByStatus(
                PageRequest.of(0, 10), userId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getContent(), hasSize(2));
        assertThat(result.getTotalElements(), is(2L));

        ApplicationDetailsDto firstApp = result.getContent().get(0);
        assertThat(firstApp.getId(), is(applicationId));
        assertThat(firstApp.getCreatorId(), is(userId));

        ApplicationDetailsDto secondApp = result.getContent().get(1);
        assertThat(secondApp.getId(), is(2L));
        assertThat(secondApp.getManagerFullName(), containsString("Петров Петр Петрович"));

        verify(applicationRepository, times(1))
                .findAllByCreatorIdOrderedByStatusAndDate(userId, PageRequest.of(0, 10));
        verify(userRepository, times(1)).findAllById(List.of(managerId));
    }

    @Test
    void findAllByManagerSortByStatus_ShouldReturnManagerApplications() {
        // Arrange
        ApplicationStatus createdStatus = mock(ApplicationStatus.class);
        when(createdStatus.getId()).thenReturn(1);

        ApplicationStatus acceptedStatus = mock(ApplicationStatus.class);
        when(acceptedStatus.getId()).thenReturn(3);

        testApplication.setManagerId(managerId);
        testApplication.setStatus(createdStatus);

        Application application2 = new Application();
        application2.setId(2L);
        application2.setCreatorId(userId);
        application2.setProjectId(templateId);
        application2.setContact("+79001234568");
        application2.setStatus(acceptedStatus);
        application2.setManagerId(managerId);
        application2.setCreatedAt(Instant.now());

        Page<Application> applicationPage = new PageImpl<>(
                List.of(testApplication, application2),
                PageRequest.of(0, 10),
                2L
        );

        when(applicationRepository.findAllByManagerIdOrderedByStatusAndDate(managerId, PageRequest.of(0, 10)))
                .thenReturn(applicationPage);
        when(userRepository.findById(managerId)).thenReturn(Optional.of(testManager));

        // Act
        Page<ApplicationDetailsDto> result = applicationService.findAllByManagerSortByStatus(
                PageRequest.of(0, 10), managerId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getContent(), hasSize(2));
        assertThat(result.getTotalElements(), is(2L));

        ApplicationDetailsDto firstApp = result.getContent().get(0);
        assertThat(firstApp.getId(), is(applicationId));
        assertThat(firstApp.getManagerId(), is(managerId));
        assertThat(firstApp.getManagerFullName(), containsString("Петров Петр Петрович"));

        verify(applicationRepository, times(1))
                .findAllByManagerIdOrderedByStatusAndDate(managerId, PageRequest.of(0, 10));
        verify(userRepository, times(1)).findById(managerId);
    }
}