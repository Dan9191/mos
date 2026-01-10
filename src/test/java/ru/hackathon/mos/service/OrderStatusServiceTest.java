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
import ru.hackathon.mos.dto.order.OrderStatusDTO;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.exception.ValidationException;
import ru.hackathon.mos.mapper.OrderMapper;
import ru.hackathon.mos.repository.*;

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
class OrderStatusServiceTest {

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusTypeRepository orderStatusTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderStatusService orderStatusService;

    private Long orderId;
    private UUID userId;
    private User testUser;
    private Order testOrder;
    private OrderStatusType newStatusType;
    private OrderStatusType documentationStatusType;
    private OrderStatusType closedStatusType;
    private ru.hackathon.mos.dto.order.OrderStatusDTO.CreateStatusRequest createRequest;

    @BeforeEach
    void setUp() {
        orderId = 1L;
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setMiddleName("Иванович");
        testUser.setEmail("ivan@example.com");

        testOrder = new Order();
        testOrder.setId(orderId);

        // Используем enum OrderStatusType.StatusName
        newStatusType = new OrderStatusType();
        newStatusType.setId(1L);
        // Предположим, что StatusName - это enum с значениями
        newStatusType.setName(OrderStatusType.StatusName.NEW);

        documentationStatusType = new OrderStatusType();
        documentationStatusType.setId(2L);
        documentationStatusType.setName(OrderStatusType.StatusName.DOCUMENTATION);

        closedStatusType = new OrderStatusType();
        closedStatusType.setId(3L);
        closedStatusType.setName(OrderStatusType.StatusName.CLOSED);

        createRequest = ru.hackathon.mos.dto.order.OrderStatusDTO.CreateStatusRequest.builder()
                .statusType("DOCUMENTATION")
                .comment("Начата подготовка документов")
                .build();
    }

    @Test
    void getAllOrderStatuses_ShouldReturnStatusList() {
        // Arrange
        OrderStatus orderStatus1 = new OrderStatus();
        orderStatus1.setId(1L);
        orderStatus1.setOrder(testOrder);
        orderStatus1.setType(newStatusType);
        orderStatus1.setComment("Заказ создан");
        orderStatus1.setChangedBy(testUser);
        orderStatus1.setCreatedAt(LocalDateTime.now().minusDays(1));

        OrderStatus orderStatus2 = new OrderStatus();
        orderStatus2.setId(2L);
        orderStatus2.setOrder(testOrder);
        orderStatus2.setType(documentationStatusType);
        orderStatus2.setComment("Начата подготовка документов");
        orderStatus2.setChangedBy(testUser);
        orderStatus2.setCreatedAt(LocalDateTime.now());

        OrderStatusDTO statusDto1 = new OrderStatusDTO();
        statusDto1.setId(1L);
        statusDto1.setOrderId(orderId);
        statusDto1.setStatusType("NEW");
        statusDto1.setComment("Заказ создан");
        statusDto1.setCreatedAt(LocalDateTime.now().minusDays(1));

        OrderStatusDTO statusDto2 = new OrderStatusDTO();
        statusDto2.setId(2L);
        statusDto2.setOrderId(orderId);
        statusDto2.setStatusType("DOCUMENTATION");
        statusDto2.setComment("Начата подготовка документов");
        statusDto2.setCreatedAt(LocalDateTime.now());

        Page<OrderStatus> statusPage = new PageImpl<>(
                List.of(orderStatus1, orderStatus2),
                PageRequest.of(0, 10),
                2L
        );

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderStatusRepository.findByOrderId(orderId, PageRequest.of(0, 10))).thenReturn(statusPage);
        when(orderMapper.toStatusDTO(orderStatus1)).thenReturn(statusDto1);
        when(orderMapper.toStatusDTO(orderStatus2)).thenReturn(statusDto2);
        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.of(orderStatus2));

        // Act
        OrderStatusDTO.StatusListResponse result = orderStatusService.getAllOrderStatuses(orderId, PageRequest.of(0, 10));

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getStatuses(), hasSize(2));
        assertThat(result.getTotal(), is(2L));
        assertThat(result.getCurrentStatus(), is("DOCUMENTATION"));

        OrderStatusDTO firstStatus = result.getStatuses().get(0);
        assertThat(firstStatus.getId(), is(1L));
        assertThat(firstStatus.getStatusType(), is("NEW"));

        OrderStatusDTO secondStatus = result.getStatuses().get(1);
        assertThat(secondStatus.getId(), is(2L));
        assertThat(secondStatus.getStatusType(), is("DOCUMENTATION"));

        verify(orderRepository, times(1)).existsById(orderId);
        verify(orderStatusRepository, times(1)).findByOrderId(orderId, PageRequest.of(0, 10));
    }

    @Test
    void getAllOrderStatuses_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.existsById(orderId)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStatusService.getAllOrderStatuses(orderId, PageRequest.of(0, 10))
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).existsById(orderId);
        verify(orderStatusRepository, never()).findByOrderId(any(), any());
    }

    @Test
    void createOrderStatus_ShouldCreateNewStatusSuccessfully() {
        // Arrange
        OrderStatus latestStatus = new OrderStatus();
        latestStatus.setId(1L);
        latestStatus.setOrder(testOrder);
        latestStatus.setType(newStatusType);
        latestStatus.setComment("Заказ создан");
        latestStatus.setChangedBy(testUser);
        latestStatus.setCreatedAt(LocalDateTime.now().minusDays(1));

        OrderStatus newOrderStatus = new OrderStatus();
        newOrderStatus.setId(2L);
        newOrderStatus.setOrder(testOrder);
        newOrderStatus.setType(documentationStatusType);
        newOrderStatus.setComment("Начата подготовка документов");
        newOrderStatus.setChangedBy(testUser);
        newOrderStatus.setCreatedAt(LocalDateTime.now());

        OrderStatusDTO expectedDto = new OrderStatusDTO();
        expectedDto.setId(2L);
        expectedDto.setOrderId(orderId);
        expectedDto.setStatusType("DOCUMENTATION");
        expectedDto.setComment("Начата подготовка документов");
        expectedDto.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStatusTypeRepository.findByName("DOCUMENTATION")).thenReturn(Optional.of(documentationStatusType));
        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.of(latestStatus));
        when(orderStatusRepository.save(any(OrderStatus.class))).thenReturn(newOrderStatus);
        when(orderMapper.toStatusDTO(newOrderStatus)).thenReturn(expectedDto);

        // Act
        OrderStatusDTO result = orderStatusService.createOrderStatus(orderId, userId, createRequest);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(2L));
        assertThat(result.getStatusType(), is("DOCUMENTATION"));
        assertThat(result.getComment(), is("Начата подготовка документов"));

        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStatusTypeRepository, times(1)).findByName("DOCUMENTATION");
        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
        verify(orderStatusRepository, times(1)).save(any(OrderStatus.class));
        verify(orderMapper, times(1)).toStatusDTO(newOrderStatus);
    }

    @Test
    void createOrderStatus_ShouldReturnExistingStatus_WhenStatusIsAlreadyCurrent() {
        // Arrange
        OrderStatus existingStatus = new OrderStatus();
        existingStatus.setId(1L);
        existingStatus.setOrder(testOrder);
        existingStatus.setType(documentationStatusType);
        existingStatus.setComment("Начата подготовка документов");
        existingStatus.setChangedBy(testUser);
        existingStatus.setCreatedAt(LocalDateTime.now());

        OrderStatusDTO existingDto = new OrderStatusDTO();
        existingDto.setId(1L);
        existingDto.setOrderId(orderId);
        existingDto.setStatusType("DOCUMENTATION");
        existingDto.setComment("Начата подготовка документов");
        existingDto.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStatusTypeRepository.findByName("DOCUMENTATION")).thenReturn(Optional.of(documentationStatusType));
        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.of(existingStatus));
        when(orderMapper.toStatusDTO(existingStatus)).thenReturn(existingDto);

        // Act
        OrderStatusDTO result = orderStatusService.createOrderStatus(orderId, userId, createRequest);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getStatusType(), is("DOCUMENTATION"));

        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStatusTypeRepository, times(1)).findByName("DOCUMENTATION");
        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
        verify(orderStatusRepository, never()).save(any(OrderStatus.class));
        verify(orderMapper, times(1)).toStatusDTO(existingStatus);
    }

    @Test
    void createOrderStatus_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStatusService.createOrderStatus(orderId, userId, createRequest)
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, never()).findById(any());
        verify(orderStatusTypeRepository, never()).findByName(any());
    }

    @Test
    void createOrderStatus_ShouldThrowException_WhenStatusTypeNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStatusTypeRepository.findByName("DOCUMENTATION")).thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> orderStatusService.createOrderStatus(orderId, userId, createRequest)
        );

        assertThat(exception.getMessage(), containsString("Тип статуса не найден: DOCUMENTATION"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStatusTypeRepository, times(1)).findByName("DOCUMENTATION");
        verify(orderStatusRepository, never()).findLatestByOrderId(any());
    }

    @Test
    void getCurrentStatus_ShouldReturnLatestStatus() {
        // Arrange
        OrderStatus latestStatus = new OrderStatus();
        latestStatus.setId(2L);
        latestStatus.setOrder(testOrder);
        latestStatus.setType(documentationStatusType);
        latestStatus.setComment("Начата подготовка документов");
        latestStatus.setChangedBy(testUser);
        latestStatus.setCreatedAt(LocalDateTime.now());

        OrderStatusDTO expectedDto = new OrderStatusDTO();
        expectedDto.setId(2L);
        expectedDto.setOrderId(orderId);
        expectedDto.setStatusType("DOCUMENTATION");
        expectedDto.setComment("Начата подготовка документов");
        expectedDto.setCreatedAt(LocalDateTime.now());

        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.of(latestStatus));
        when(orderMapper.toStatusDTO(latestStatus)).thenReturn(expectedDto);

        // Act
        OrderStatusDTO result = orderStatusService.getCurrentStatus(orderId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(2L));
        assertThat(result.getStatusType(), is("DOCUMENTATION"));
        assertThat(result.getComment(), is("Начата подготовка документов"));

        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
        verify(orderMapper, times(1)).toStatusDTO(latestStatus);
    }

    @Test
    void getCurrentStatus_ShouldThrowException_WhenNoStatusFound() {
        // Arrange
        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStatusService.getCurrentStatus(orderId)
        );

        assertThat(exception.getMessage(), containsString("Статус не найден для заказа ID: " + orderId));
        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
        verify(orderMapper, never()).toStatusDTO(any());
    }

    @Test
    void canChangeToStatus_ShouldReturnTrue_WhenOrderNotClosed() {
        // Arrange
        OrderStatus currentStatus = new OrderStatus();
        currentStatus.setId(2L);
        currentStatus.setOrder(testOrder);
        currentStatus.setType(documentationStatusType);
        currentStatus.setChangedBy(testUser);
        currentStatus.setCreatedAt(LocalDateTime.now());

        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.of(currentStatus));

        // Act
        boolean result = orderStatusService.canChangeToStatus(orderId, "IN_PROGRESS");

        // Assert
        assertThat(result, is(true));
        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
    }

    @Test
    void canChangeToStatus_ShouldReturnTrue_WhenNoStatusExists() {
        // Arrange
        when(orderStatusRepository.findLatestByOrderId(orderId)).thenReturn(Optional.empty());

        // Act
        boolean result = orderStatusService.canChangeToStatus(orderId, "NEW");

        // Assert
        assertThat(result, is(true));
        verify(orderStatusRepository, times(1)).findLatestByOrderId(orderId);
    }
}