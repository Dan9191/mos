package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.order.OrderStageDTO;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.OrderStage;
import ru.hackathon.mos.entity.OrderStageType;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.exception.ValidationException;
import ru.hackathon.mos.mapper.OrderMapper;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.OrderStageRepository;
import ru.hackathon.mos.repository.OrderStageTypeRepository;
import ru.hackathon.mos.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStageService {

    private final OrderStageRepository orderStageRepository;
    private final OrderRepository orderRepository;
    private final OrderStageTypeRepository orderStageTypeRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    /**
     * Получить историю этапов строительства по заказу
     */
    public OrderStageDTO.StageListResponse getOrderStages(Long orderId, Pageable pageable) {
        log.info("Получение этапов для заказа ID: {}", orderId);

        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Заказ не найден");
        }

        Page<OrderStage> stagesPage = orderStageRepository.findByOrderId(orderId, pageable);

        List<OrderStageDTO> stageDTOs = stagesPage.getContent().stream()
                .map(orderMapper::toStageDTO)
                .collect(Collectors.toList());

        Long activeCount = orderStageRepository.countActiveStages(orderId);
        Long completedCount = orderStageRepository.countCompletedStages(orderId);

        return OrderStageDTO.StageListResponse.builder()
                .stages(stageDTOs)
                .total(stagesPage.getTotalElements())
                .activeCount(activeCount)
                .completedCount(completedCount)
                .build();
    }

    /**
     * Создать этап строительства с полной идемпотентностью
     */
    @Transactional
    public OrderStageDTO createOrderStage(Long orderId,
                                          UUID userId,
                                          OrderStageDTO.CreateStageRequest request) {
        log.info("Создание этапа для заказа ID: {}, тип: {}", orderId, request.getStageType());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        OrderStageType stageType = orderStageTypeRepository.findByName(request.getStageType())
                .orElseThrow(() -> new ValidationException("Тип этапа не найден"));

        // Проверяем, были ли этапы такого типа вообще в истории заказа
        List<OrderStage> existingStages = orderStageRepository.findByOrderIdAndTypeName(orderId, request.getStageType());

        // Если этап такого типа уже был в истории
        if (!existingStages.isEmpty()) {
            // Находим последний этап такого типа (не важно, активный или завершенный)
            OrderStage lastStage = existingStages.stream()
                    .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                    .orElse(null);

            if (lastStage != null) {
                log.info("Этап '{}' уже был в истории заказа ID: {}, возвращаем существующий (ID: {})",
                        request.getStageType(), orderId, lastStage.getId());
                return orderMapper.toStageDTO(lastStage);
            }
        }

        // Создаем новый этап
        OrderStage orderStage = OrderStage.builder()
                .order(order)
                .type(stageType)
                .changedBy(user)
                .createdAt(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .plannedEndDate(request.getPlannedEndDate())
                .isCompleted(false)
                .progress(request.getProgress())
                .notes(request.getDescription())
                .build();

        OrderStage savedStage = orderStageRepository.save(orderStage);
        log.info("Создан этап '{}' (ID: {}) для заказа ID: {}",
                stageType.getName(), savedStage.getId(), orderId);

        return orderMapper.toStageDTO(savedStage);
    }

    /**
     * Обновить этап строительства
     */
    @Transactional
    public OrderStageDTO updateOrderStage(Long stageId,
                                          UUID userId,
                                          OrderStageDTO.UpdateStageRequest request) {
        log.info("Обновление этапа ID: {}", stageId);

        OrderStage orderStage = orderStageRepository.findById(stageId)
                .orElseThrow(() -> new NotFoundException("Этап не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (request.getStatus() != null) {
            if ("completed".equals(request.getStatus())) {
                orderStage.setIsCompleted(true);
                orderStage.setCompletionDate(request.getActualEndDate() != null ?
                        request.getActualEndDate() : LocalDateTime.now());
            }
        }

        if (request.getProgress() != null) {
            if (request.getProgress() < 0 || request.getProgress() > 100) {
                throw new ValidationException("Прогресс должен быть в диапазоне 0-100");
            }
            orderStage.setProgress(request.getProgress());
        }

        if (request.getActualEndDate() != null) {
            orderStage.setCompletionDate(request.getActualEndDate());
        }

        OrderStage updatedStage = orderStageRepository.save(orderStage);
        log.info("Этап ID: {} обновлен", stageId);

        return orderMapper.toStageDTO(updatedStage);
    }

    /**
     * Получить текущий активный этап
     */
    public OrderStageDTO getCurrentStage(Long orderId) {

        OrderStage stage = orderStageRepository.findCurrentStageByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Активный этап не найден"));

        return orderMapper.toStageDTO(stage);
    }

    /**
     * Удалить текущий активный этап
     */
    @Transactional
    public void deleteCurrentStage(Long stageId) {
        log.info("Удаление этапа ID: {}", stageId);

        OrderStage orderStage = orderStageRepository.findById(stageId)
                .orElseThrow(() -> new NotFoundException("Этап не найден"));

        orderStageRepository.delete(orderStage);
        log.info("Этап ID: {} удален", stageId);
    }
}