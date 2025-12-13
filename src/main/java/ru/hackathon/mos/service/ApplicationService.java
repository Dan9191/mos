package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.UUID;

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

    /**
     * Создать заявку. Заявка создается пользователем.
     *
     * @param templateId ID шаблона-проекта.
     * @param userUuid   ID пользователя.
     * @return созданная заявка
     */
    @Transactional
    public ApplicationDetailsDto createApplication(Long templateId, String userUuid) {
        ProjectTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        ApplicationStatus status = statusRepository.findById(CREATED.getId())
                .orElseThrow(() -> new RuntimeException("Status 'created' not found"));

        Application app = new Application();
        app.setCreatorId(UUID.fromString(userUuid));
        app.setStatus(status);
        app.setManagerId(null);
        app.setCreatedAt(java.time.Instant.now());
        app.setProjectId(template.getId());
        log.info("Creating application {}", app);
        applicationRepository.save(app);
        return new ApplicationDetailsDto(app);
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

        app.setManagerId(UUID.fromString(managerUuid));
        app.setStatus(status);
        log.info("Taking application {}", app);
        applicationRepository.save(app);
        return new ApplicationDetailsDto(app);
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

        app.setManagerId(UUID.fromString(managerUuid));
        app.setStatus(status);
        log.info("Rejected application {}", app);
        applicationRepository.save(app);
        return new ApplicationDetailsDto(app);
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

        app.setManagerId(UUID.fromString(managerUuid));
        app.setStatus(status);

        Application savedApp = applicationRepository.save(app);

        // создаём Order
        Order order = Order.builder()
                .client(client)
                .managerId(UUID.fromString(managerUuid))
                .project(project)
                .address("Временно не заполнено")
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);
        log.info("Accepted application {}", app);
        log.info("Order id: '{}' created", savedApp);
        return new ApplicationDetailsDto(savedApp);
    }
}
