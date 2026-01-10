package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.hackathon.mos.dto.order.CreateOrderRequest;
import ru.hackathon.mos.dto.order.OrderDTO;
import ru.hackathon.mos.dto.order.OrderUpdateRequest;
import ru.hackathon.mos.dto.order.OrderStatusDTO;
import ru.hackathon.mos.dto.order.OrderStageDTO;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.exception.AccessDeniedException;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.exception.ValidationException;
import ru.hackathon.mos.mapper.OrderMapper;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.OrderStageRepository;
import ru.hackathon.mos.repository.OrderStatusRepository;
import ru.hackathon.mos.repository.OrderStatusTypeRepository;
import ru.hackathon.mos.repository.ProjectTemplateRepository;
import ru.hackathon.mos.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private OrderStageRepository orderStageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectTemplateRepository projectTemplateRepository;

    @Mock
    private OrderStatusTypeRepository orderStatusTypeRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID managerId;
    private Long orderId;
    private Long projectId;
    private User testUser;
    private User testManager;
    private Order testOrder;
    private ProjectTemplate testProject;
    private OrderStatusType newStatusType;
    private OrderStatus currentOrderStatus;
    private OrderStage currentOrderStage;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        managerId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        orderId = 1L;
        projectId = 100L;

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setMiddleName("Иванович");
        testUser.setEmail("ivan@example.com");

        testManager = new User();
        testManager.setId(managerId);
        testManager.setFirstName("Петр");
        testManager.setLastName("Петров");
        testManager.setMiddleName("Петрович");
        testManager.setEmail("manager@example.com");

        testProject = new ProjectTemplate();
        testProject.setId(projectId);
        testProject.setTitle("Современный дом 120м²");
        testProject.setBasePrice(java.math.BigDecimal.valueOf(5000000));
        testProject.setAreaM2(120.5);
        testProject.setIsActive(true);

        testOrder = new Order();
        testOrder.setId(orderId);
        testOrder.setClient(testUser);
        testOrder.setManagerId(managerId);
        testOrder.setProject(testProject);
        testOrder.setAddress("Москва, ул. Строителей, 1");
        testOrder.setCreatedAt(LocalDateTime.now());

        newStatusType = new OrderStatusType();
        newStatusType.setId(1L);
        // Предположим, что есть метод для установки имени
        // newStatusType.setName(OrderStatusType.StatusName.NEW);

        currentOrderStatus = new OrderStatus();
        currentOrderStatus.setId(1L);
        currentOrderStatus.setOrder(testOrder);
        currentOrderStatus.setType(newStatusType);
        currentOrderStatus.setChangedBy(testUser);
        currentOrderStatus.setCreatedAt(LocalDateTime.now());

        currentOrderStage = new OrderStage();
        currentOrderStage.setId(1L);
        currentOrderStage.setOrder(testOrder);
        currentOrderStage.setIsCompleted(false);
        currentOrderStage.setProgress(25);
        currentOrderStage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getOrdersByUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderService.getOrdersByUser(userId, PageRequest.of(0, 10))
        );

        assertThat(exception.getMessage(), containsString("Пользователь не найден"));
        verify(userRepository, times(1)).findById(userId);
        verify(orderRepository, never()).findByUserId(any(), any());
    }

    @Test
    void getOrderById_ShouldReturnOrderDetails() {
        // Arrange
        OrderDTO orderDto = new OrderDTO();
        orderDto.setId(orderId);
        orderDto.setAddress("Москва, ул. Строителей, 1");
        orderDto.setManagerId(managerId);

        OrderStatusDTO statusDto = new OrderStatusDTO();
        statusDto.setId(1L);
        statusDto.setStatusType("NEW");

        OrderStageDTO stageDto = new OrderStageDTO();
        stageDto.setId(1L);
        stageDto.setStageType("FOUNDATION");
        stageDto.setProgress(25);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(testManager));
        when(orderMapper.toDTO(testOrder)).thenReturn(orderDto);
        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.of(currentOrderStatus));
        when(orderStageRepository.findCurrentStageByOrderId(orderId)).thenReturn(Optional.of(currentOrderStage));
        when(orderMapper.toStatusDTO(currentOrderStatus)).thenReturn(statusDto);
        when(orderMapper.toStageDTO(currentOrderStage)).thenReturn(stageDto);

        // Act
        OrderDTO result = orderService.getOrderById(orderId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(orderId));
        assertThat(result.getCurrentStatus(), is(notNullValue()));
        assertThat(result.getCurrentStage(), is(notNullValue()));

        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(managerId);
        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
        verify(orderStageRepository, times(1)).findCurrentStageByOrderId(orderId);
    }

    @Test
    void getOrderById_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProjectId(projectId);
        request.setAddress("Москва, ул. Строителей, 1");

        OrderStatus initialStatus = new OrderStatus();
        initialStatus.setId(1L);
        initialStatus.setOrder(testOrder);
        initialStatus.setType(newStatusType);
        initialStatus.setChangedBy(testUser);
        initialStatus.setCreatedAt(LocalDateTime.now());

        OrderDTO orderDto = new OrderDTO();
        orderDto.setId(orderId);
        orderDto.setAddress("Москва, ул. Строителей, 1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(projectTemplateRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderStatusTypeRepository.findByName("NEW")).thenReturn(Optional.of(newStatusType));
        when(orderStatusRepository.save(any(OrderStatus.class))).thenReturn(initialStatus);
        when(orderMapper.toDTO(testOrder)).thenReturn(orderDto);

        // Act
        OrderDTO result = orderService.createOrder(userId, managerId, request);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(orderId));
        assertThat(result.getAddress(), is("Москва, ул. Строителей, 1"));

        verify(userRepository, times(1)).findById(userId);
        verify(projectTemplateRepository, times(1)).findById(projectId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderStatusTypeRepository, times(1)).findByName("NEW");
        verify(orderStatusRepository, times(1)).save(any(OrderStatus.class));
        verify(orderMapper, times(1)).toDTO(testOrder);
    }

    @Test
    void createOrder_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProjectId(projectId);
        request.setAddress("Москва, ул. Строителей, 1");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderService.createOrder(userId, managerId, request)
        );

        assertThat(exception.getMessage(), containsString("Пользователь не найден"));
        verify(userRepository, times(1)).findById(userId);
        verify(projectTemplateRepository, never()).findById(any());
    }

    @Test
    void createOrder_ShouldThrowException_WhenProjectNotFound() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProjectId(projectId);
        request.setAddress("Москва, ул. Строителей, 1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(projectTemplateRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderService.createOrder(userId, managerId, request)
        );

        assertThat(exception.getMessage(), containsString("Проект не найден"));
        verify(userRepository, times(1)).findById(userId);
        verify(projectTemplateRepository, times(1)).findById(projectId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_ShouldThrowException_WhenProjectNotActive() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProjectId(projectId);
        request.setAddress("Москва, ул. Строителей, 1");

        testProject.setIsActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(projectTemplateRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> orderService.createOrder(userId, managerId, request)
        );

        assertThat(exception.getMessage(), containsString("Проект не активен"));
        verify(userRepository, times(1)).findById(userId);
        verify(projectTemplateRepository, times(1)).findById(projectId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setAddress("Москва, ул. Обновленная, 10");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderService.updateOrder(orderId, request)
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkOrderAccess_ShouldAllowAccess_WhenUserOwnsOrder() {
        // Arrange
        when(orderRepository.existsByOrderIdAndUserId(orderId, userId)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> orderService.checkOrderAccess(orderId, userId));

        verify(orderRepository, times(1)).existsByOrderIdAndUserId(orderId, userId);
    }

    @Test
    void checkOrderAccess_ShouldThrowException_WhenUserDoesNotOwnOrder() {
        // Arrange
        when(orderRepository.existsByOrderIdAndUserId(orderId, userId)).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> orderService.checkOrderAccess(orderId, userId)
        );

        assertThat(exception.getMessage(), containsString("Доступ к заказу запрещен"));
        verify(orderRepository, times(1)).existsByOrderIdAndUserId(orderId, userId);
    }

    @Test
    void deleteOrderById_ShouldDeleteOrderSuccessfully() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).delete(testOrder);

        // Act
        orderService.deleteOrderById(orderId);

        // Assert
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void deleteOrderById_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderService.deleteOrderById(orderId)
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).delete(any());
    }
}