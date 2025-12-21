package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.order.OrderStatusDTO;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.OrderStatus;
import ru.hackathon.mos.entity.OrderStatusType;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.exception.ValidationException;
import ru.hackathon.mos.mapper.OrderMapper;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.OrderStatusRepository;
import ru.hackathon.mos.repository.OrderStatusTypeRepository;
import ru.hackathon.mos.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusTypeRepository orderStatusTypeRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    /**
     * Получить все статусы заказа
     */
    public OrderStatusDTO.StatusListResponse getAllOrderStatuses(Long orderId, Pageable pageable) {
        log.info("Получение статусов для заказа ID: {}", orderId);

        // Проверяем существование заказа
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Заказ не найден");
        }

        // Получаем страницу статусов
        Page<OrderStatus> statusesPage = orderStatusRepository.findByOrderId(orderId, pageable);

        // Преобразуем в DTO
        List<OrderStatusDTO> statusDTOs = statusesPage.getContent().stream()
                .map(orderMapper::toStatusDTO)
                .collect(Collectors.toList());

        // Получаем текущий статус
        String currentStatus = orderStatusRepository.findLatestByOrderId(orderId)
                .map(status -> status.getType().getName().toString())
                .orElse("unknown");

        // Создаем и возвращаем StatusListResponse
        return OrderStatusDTO.StatusListResponse.builder()
                .statuses(statusDTOs)
                .total(statusesPage.getTotalElements())
                .currentStatus(currentStatus)
                .build();
    }

    /**
     * Создать новый статус для заказа с идемпотентностью
     */
    @Transactional
    public OrderStatusDTO createOrderStatus(Long orderId, UUID userId,
                                            OrderStatusDTO.CreateStatusRequest request) {
        log.info("Создание статуса для заказа ID: {}, тип: {}", orderId, request.getStatusType());

        // 1. Проверяем существование заказа
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        // 2. Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // 3. Находим тип статуса
        String statusTypeStr = request.getStatusType().toUpperCase();
        OrderStatusType statusType = orderStatusTypeRepository.findByName(statusTypeStr)
                .orElseThrow(() -> new ValidationException("Тип статуса не найден: " + request.getStatusType()));

        // 4. Получаем ПОСЛЕДНИЙ статус
        OrderStatus latestStatus = orderStatusRepository.findLatestByOrderId(orderId).orElse(null);

        // 5. ПРОВЕРКА 1: Если запрашиваемый статус уже последний - идемпотентность
        if (latestStatus != null &&
                latestStatus.getType().getName().toString().equalsIgnoreCase(statusTypeStr)) {
            log.info("Статус '{}' уже является текущим для заказа ID: {}, возвращаем существующий (ID: {})",
                    statusTypeStr, orderId, latestStatus.getId());
            return orderMapper.toStatusDTO(latestStatus);
        }

        // 6. ПРОВЕРКА 2: Проверяем, был ли такой статус уже в истории заказа
        boolean statusAlreadyExists = orderStatusRepository.existsByOrderIdAndType(orderId, statusTypeStr);

        if (statusAlreadyExists) {
            // Получаем последний статус этого типа для логирования
            List<OrderStatus> sameTypeStatuses = orderStatusRepository.findByOrderIdAndType(orderId, statusTypeStr);
            if (!sameTypeStatuses.isEmpty()) {
                OrderStatus lastSameTypeStatus = sameTypeStatuses.stream()
                        .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                        .orElse(null);

                if (lastSameTypeStatus != null) {
                    throw new ValidationException(
                            String.format("Статус '%s' уже существует в истории заказа. " +
                                            "Последний раз был установлен: %s",
                                    request.getStatusType(), lastSameTypeStatus.getCreatedAt()));
                }
            }
        }

        // 7. СОЗДАНИЕ нового статуса
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrder(order);
        orderStatus.setType(statusType);
        orderStatus.setChangedBy(user);
        orderStatus.setCreatedAt(LocalDateTime.now());

        OrderStatus savedStatus = orderStatusRepository.save(orderStatus);

        log.info("Создан статус '{}' (ID: {}) для заказа ID: {}",
                statusType.getName(), savedStatus.getId(), orderId);

        return orderMapper.toStatusDTO(savedStatus);
    }

    /**
     * Получить текущий статус заказа
     */
    public OrderStatusDTO getCurrentStatus(Long orderId) {
        log.info("Получение текущего статуса для заказа ID: {}", orderId);

        OrderStatus status = orderStatusRepository.findLatestByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Статус не найден для заказа ID: " + orderId));

        return orderMapper.toStatusDTO(status);
    }

    /**
     * Проверить возможность установки статуса
     */
    public boolean canChangeToStatus(Long orderId, String statusType) {
        log.info("Проверка возможности изменения статуса для заказа ID: {} на статус: {}", orderId, statusType);

        OrderStatus currentStatus = orderStatusRepository.findLatestByOrderId(orderId).orElse(null);
        if (currentStatus != null && "closed".equals(currentStatus.getType().getName().toString())) {
            log.warn("Заказ ID: {} уже закрыт, изменение статуса запрещено", orderId);
            return false;
        }

        return true;
    }
}