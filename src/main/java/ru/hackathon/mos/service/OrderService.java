package ru.hackathon.mos.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.common.ListResponse;
import ru.hackathon.mos.dto.order.CreateOrderRequest;
import ru.hackathon.mos.dto.order.OrderDTO;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.exception.AccessDeniedException;
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
@Builder
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderStageRepository orderStageRepository;
    private final UserRepository userRepository;
    private final ProjectTemplateRepository projectTemplateRepository;
    private final OrderStatusTypeRepository orderStatusTypeRepository;
    private final OrderMapper orderMapper;

    /**
     * Получить список заказов пользователя
     */
    public ListResponse<OrderDTO> getOrdersByUser(UUID userId, Pageable pageable) {
        log.info("Получение заказов для пользователя ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Page<Order> ordersPage = orderRepository.findByUserId(user.getId(), pageable);

        List<OrderDTO> orderDTOs = ordersPage.getContent().stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDTO(order);
                    // Добавляем текущий статус
                    orderStatusRepository.findLatestByOrderId(order.getId())
                            .ifPresent(status -> dto.setCurrentStatus(orderMapper.toStatusDTO(status)));
                    // Добавляем текущий этап
                    orderStageRepository.findCurrentStageByOrderId(order.getId())
                            .ifPresent(stage -> dto.setCurrentStage(orderMapper.toStageDTO(stage)));
                    return dto;
                })
                .collect(Collectors.toList());

        return ListResponse.<OrderDTO>builder()
                .items(orderDTOs)
                .total(ordersPage.getTotalElements())
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .build();
    }

    /**
     * Получить информацию о заказе
     */
    public OrderDTO getOrderById(Long orderId) {
        log.info("Получение информации о заказе ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        OrderDTO dto = orderMapper.toDTO(order);

        orderStatusRepository.findLatestByOrderId(orderId)
                .ifPresent(status -> dto.setCurrentStatus(orderMapper.toStatusDTO(status)));

        orderStageRepository.findCurrentStageByOrderId(orderId)
                .ifPresent(stage -> dto.setCurrentStage(orderMapper.toStageDTO(stage)));

        return dto;
    }

    /**
     * Создать новый заказ
     */
    @Transactional
    public OrderDTO createOrder(UUID userId, CreateOrderRequest request) {
        log.info("Создание заказа для пользователя ID: {}", userId);

        User client = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ProjectTemplate project = projectTemplateRepository.findById(request.getProjectId())
                .orElseThrow(() -> new NotFoundException("Проект не найден"));

        if (!project.getIsActive()) {
            throw new ValidationException("Проект не активен");
        }

        Order order = Order.builder()
                .client(client)
                .project(project)
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Заказ создан с ID: {}", savedOrder.getId());

        createInitialStatus(savedOrder, client);

        return orderMapper.toDTO(savedOrder);
    }

    /**
     * Проверить доступ пользователя к заказу
     */
    public void checkOrderAccess(Long orderId, UUID userId) {
        boolean hasAccess = orderRepository.existsByOrderIdAndUserId(orderId, userId);
        if (!hasAccess) {
            throw new AccessDeniedException("Доступ к заказу запрещен");
        }
    }

    private void createInitialStatus(Order order, User user) {

        String statusName = OrderStatusType.StatusName.NEW.getValue();

        OrderStatusType statusType = orderStatusTypeRepository.findByName(statusName)
                .orElseThrow(() -> new ValidationException("Тип статуса 'new' не найден"));

        OrderStatus initialStatus = new OrderStatus();
        initialStatus.setOrder(order);
        initialStatus.setType(statusType);
        initialStatus.setChangedBy(user);
        initialStatus.setCreatedAt(LocalDateTime.now());

        orderStatusRepository.save(initialStatus);
        log.info("Создан начальный статус 'new' для заказа ID: {}", order.getId());
    }
}