package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.order.OrderStatusDTO;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.exception.ValidationException;
import ru.hackathon.mos.mapper.OrderMapper;
import ru.hackathon.mos.repository.*;

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
     * Создать новый статус для заказа
     */
    @Transactional
    public OrderStatusDTO createOrderStatus(Long orderId, UUID userId,
                                            OrderStatusDTO.CreateStatusRequest request) {
        log.info("Создание статуса для заказа ID: {}", orderId);

        // Находим заказ
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        // Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Находим тип статуса по имени из запроса
        OrderStatusType statusType = orderStatusTypeRepository.findByName(request.getStatusType())
                .orElseThrow(() -> new ValidationException("Тип статуса не найден: " + request.getStatusType()));

        // Проверяем, не установлен ли уже такой статус
        OrderStatus latestStatus = orderStatusRepository.findLatestByOrderId(orderId).orElse(null);
        if (latestStatus != null && latestStatus.getType().getName().toString().equals(request.getStatusType())) {
            throw new ValidationException("Статус '" + request.getStatusType() + "' уже установлен");
        }

        // Создаем новый статус
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrder(order);
        orderStatus.setType(statusType);
        orderStatus.setChangedBy(user);
        orderStatus.setCreatedAt(LocalDateTime.now());

        // Сохраняем статус
        OrderStatus savedStatus = orderStatusRepository.save(orderStatus);
        log.info("Создан статус '{}' для заказа ID: {}", statusType.getName(), orderId);

        // Возвращаем DTO
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