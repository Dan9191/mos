package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.application.ApplicationCreateRequest;
import ru.hackathon.mos.dto.application.ApplicationDetailsDto;
import ru.hackathon.mos.entity.Application;
import ru.hackathon.mos.entity.ApplicationStatus;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.ProjectTemplate;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.repository.ApplicationRepository;
import ru.hackathon.mos.repository.ApplicationStatusRepository;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.ProjectTemplateRepository;
import ru.hackathon.mos.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.hackathon.mos.dto.ApplicationStatusEnum.ACCEPTED;
import static ru.hackathon.mos.dto.ApplicationStatusEnum.CONSIDERATION;
import static ru.hackathon.mos.dto.ApplicationStatusEnum.CREATED;
import static ru.hackathon.mos.dto.ApplicationStatusEnum.REJECTED;

/**
 * Сервис работы с заявками.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepository;

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository statusRepository;
    private final ProjectTemplateRepository templateRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;


    /**
     * Получение страницы с заявками. Заявки отсортированный по дате и статусу.
     * Порядок статусов created, consideration, accepted, rejected.
     *
     * @param pageable Параметры страницы.
     * @return страница с запросами.
     */
    @Transactional
    public Page<ApplicationDetailsDto> findAllSortByStatus(Pageable pageable) {
        Page<Application> page = applicationRepository.findAllOrderedByStatusAndDate(pageable);

        List<UUID> managerIds = extractManagerIds(page.getContent());

        Map<UUID, User> managersMap = loadManagers(managerIds);

        List<ApplicationDetailsDto> dtos = convertApplicationsToDtos(page.getContent(), managersMap);

        log.info("Find all applications");
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    /**
     * Создать заявку. Заявка создается пользователем.
     *
     * @param request  Данные для заявки.
     * @param userUuid ID пользователя.
     * @return созданная заявка
     */
    @Transactional
    public ApplicationDetailsDto createApplication(ApplicationCreateRequest request, String userUuid) {
        ProjectTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        ApplicationStatus status = statusRepository.findById(CREATED.getId())
                .orElseThrow(() -> new RuntimeException("Status 'created' not found"));

        Application app = new Application();
        app.setCreatorId(UUID.fromString(userUuid));
        app.setStatus(status);
        app.setContact(request.getContact());
        app.setManagerId(null);
        app.setCreatedAt(java.time.Instant.now());
        app.setProjectId(template.getId());

        Application savedApp = applicationRepository.save(app);
        log.info("Creating application {}", savedApp.getId());

        return convertToDto(savedApp, null);
    }

    /**
     * Взять заявку в обработку.
     *
     * @param applicationId ID заявки.
     * @param managerUuid   ID менеджера.
     * @return обновленная заявка.
     */
    @Transactional
    public ApplicationDetailsDto takeApplication(Long applicationId, String managerUuid) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ApplicationStatus status = statusRepository.findById(CONSIDERATION.getId())
                .orElseThrow(() -> new RuntimeException("Status 'consideration' not found"));

        User manager = userRepository.findById(UUID.fromString(managerUuid))
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        app.setManagerId(manager.getId());
        app.setStatus(status);

        Application savedApp = applicationRepository.save(app);
        log.info("Taking application {}", savedApp.getId());

        return convertToDto(savedApp, manager);
    }

    /**
     * Отклонение заявки
     *
     * @param applicationId ID заявки.
     * @param managerUuid   ID менеджера.
     * @return обновленная заявка.
     */
    @Transactional
    public ApplicationDetailsDto rejectApplication(Long applicationId, String managerUuid) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ApplicationStatus status = statusRepository.findById(REJECTED.getId())
                .orElseThrow(() -> new RuntimeException("Status 'rejected' not found"));

        User manager = userRepository.findById(UUID.fromString(managerUuid))
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        app.setManagerId(manager.getId());
        app.setStatus(status);

        Application savedApp = applicationRepository.save(app);
        log.info("Rejected application {}", savedApp.getId());

        return convertToDto(savedApp, manager);
    }

    /**
     * Перевод заявки в статус "Заявка принята". Параллельно создается новый заказ.
     *
     * @param applicationId ID заявки.
     * @param managerUuid   ID менеджера
     * @return обновленная заявка.
     */
    @Transactional
    public ApplicationDetailsDto acceptApplication(Long applicationId, String managerUuid) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ProjectTemplate project = templateRepository.findById(app.getProjectId())
                .orElseThrow(() -> new RuntimeException("ProjectTemplate not found"));

        ApplicationStatus status = statusRepository.findById(ACCEPTED.getId())
                .orElseThrow(() -> new RuntimeException("Status 'accepted' not found"));

        User client = userRepository.findById(app.getCreatorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User manager = userRepository.findById(UUID.fromString(managerUuid))
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        app.setManagerId(UUID.fromString(managerUuid));
        app.setStatus(status);

        Application savedApp = applicationRepository.save(app);

        // создаём Order
        Order order = Order.builder()
                .client(client)
                .managerId(UUID.fromString(managerUuid))
                .project(project)
                .clientContact(app.getContact())
                .address("Временно не заполнено")
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        orderService.createInitialStatus(order, manager);

        log.info("Accepted application {}", savedApp.getId());
        log.info("Order id: '{}' created", order.getId());

        return convertToDto(savedApp, manager);
    }

    /**
     * Получение страницы с заявками пользователя. Заявки отсортированный по дате и статусу.
     * Порядок статусов created, consideration, accepted, rejected.
     *
     * @param pageable Параметры страницы.
     * @param userId   ID пользователя.
     * @return страница с заявками.
     */
    public Page<ApplicationDetailsDto> findAllByUserSortByStatus(Pageable pageable, UUID userId) {
        Page<Application> page = applicationRepository.findAllByCreatorIdOrderedByStatusAndDate(userId, pageable);

        List<UUID> managerIds = extractManagerIds(page.getContent());
        Map<UUID, User> managersMap = loadManagers(managerIds);

        List<ApplicationDetailsDto> dtos = convertApplicationsToDtos(page.getContent(), managersMap);

        log.info("Find all applications by user {}", userId);
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }



    /**
     * Получение страницы с заявками, курируемые менеджером. Заявки отсортированный по дате и статусу.
     * Порядок статусов created, consideration, accepted, rejected.
     *
     * @param pageable  Параметры страницы.
     * @param managerId ID пользователя.
     * @return страница с заявками.
     */
    public Page<ApplicationDetailsDto> findAllByManagerSortByStatus(Pageable pageable, UUID managerId) {
        Page<Application> page = applicationRepository.findAllByManagerIdOrderedByStatusAndDate(managerId, pageable);

        User manager = userRepository.findById(managerId).orElse(null);
        Map<UUID, User> managersMap = new HashMap<>();
        if (manager != null) {
            managersMap.put(managerId, manager);
        }

        List<ApplicationDetailsDto> dtos = convertApplicationsToDtos(page.getContent(), managersMap);

        log.info("Find all applications by manager Id {}", managerId);
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    // =============== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===============

    private List<UUID> extractManagerIds(List<Application> applications) {
        return applications.stream()
                .filter(app -> app.getManagerId() != null)
                .map(Application::getManagerId)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<UUID, User> loadManagers(List<UUID> managerIds) {
        if (managerIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(managerIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private List<ApplicationDetailsDto> convertApplicationsToDtos(List<Application> applications, Map<UUID, User> managersMap) {
        return applications.stream()
                .map(app -> {
                    User manager = managersMap.get(app.getManagerId());
                    return convertToDto(app, manager);
                })
                .collect(Collectors.toList());
    }

    private ApplicationDetailsDto convertToDto(Application application, User manager) {
        return new ApplicationDetailsDto(application, manager);
    }
}
