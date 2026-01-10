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
import ru.hackathon.mos.dto.order.OrderStageDTO;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.exception.ValidationException;
import ru.hackathon.mos.mapper.OrderMapper;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.OrderStageRepository;
import ru.hackathon.mos.repository.OrderStageTypeRepository;
import ru.hackathon.mos.repository.UserRepository;

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
class OrderStageServiceTest {

    @Mock
    private OrderStageRepository orderStageRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStageTypeRepository orderStageTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderStageService orderStageService;

    private Long orderId;
    private Long stageId;
    private UUID userId;
    private User testUser;
    private Order testOrder;
    private OrderStageType foundationStageType;
    private OrderStageType framingStageType;
    private OrderStageDTO.CreateStageRequest createRequest;
    private OrderStageDTO.UpdateStageRequest updateRequest;

    @BeforeEach
    void setUp() {
        orderId = 1L;
        stageId = 10L;
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setEmail("ivan@example.com");

        testOrder = new Order();
        testOrder.setId(orderId);

        foundationStageType = new OrderStageType();
        foundationStageType.setId(1L);
        foundationStageType.setName(OrderStageType.StageName.FOUNDATION);
        foundationStageType.setDescription("Закладка фундамента");

        framingStageType = new OrderStageType();
        framingStageType.setId(2L);
        framingStageType.setName(OrderStageType.StageName.WALL_PREPARATION);
        framingStageType.setDescription("Возведение каркаса");

        createRequest = new OrderStageDTO.CreateStageRequest();
        createRequest.setStageType("FOUNDATION");
        createRequest.setDescription("Начата закладка фундамента");
        createRequest.setProgress(25);
        createRequest.setPlannedEndDate(LocalDateTime.now().plusDays(30));

        updateRequest = new OrderStageDTO.UpdateStageRequest();
        updateRequest.setProgress(50);
        updateRequest.setStatus("completed");
        updateRequest.setActualEndDate(LocalDateTime.now());
    }

    @Test
    void getOrderStages_ShouldReturnStageListResponse() {
        // Arrange
        OrderStage stage1 = OrderStage.builder()
                .id(1L)
                .order(testOrder)
                .type(foundationStageType)
                .notes("Начата закладка фундамента")
                .progress(25)
                .isCompleted(false)
                .startDate(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        OrderStage stage2 = OrderStage.builder()
                .id(2L)
                .order(testOrder)
                .type(framingStageType)
                .notes("Возведение каркаса начато")
                .progress(10)
                .isCompleted(false)
                .startDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        OrderStageDTO stageDto1 = new OrderStageDTO();
        stageDto1.setId(1L);
        stageDto1.setOrderId(orderId);
        stageDto1.setStageType("FOUNDATION");
        stageDto1.setDescription("Начата закладка фундамента");
        stageDto1.setProgress(25);
        stageDto1.setStatus("in_progress");

        OrderStageDTO stageDto2 = new OrderStageDTO();
        stageDto2.setId(2L);
        stageDto2.setOrderId(orderId);
        stageDto2.setStageType("FRAMING");
        stageDto2.setDescription("Возведение каркаса начато");
        stageDto2.setProgress(10);
        stageDto2.setStatus("in_progress");

        Page<OrderStage> stagesPage = new PageImpl<>(
                List.of(stage1, stage2),
                PageRequest.of(0, 10),
                2L
        );

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderStageRepository.findByOrderId(orderId, PageRequest.of(0, 10))).thenReturn(stagesPage);
        when(orderMapper.toStageDTO(stage1)).thenReturn(stageDto1);
        when(orderMapper.toStageDTO(stage2)).thenReturn(stageDto2);
        when(orderStageRepository.countActiveStages(orderId)).thenReturn(2L);
        when(orderStageRepository.countCompletedStages(orderId)).thenReturn(0L);

        // Act
        OrderStageDTO.StageListResponse result = orderStageService.getOrderStages(orderId, PageRequest.of(0, 10));

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getStages(), hasSize(2));
        assertThat(result.getTotal(), is(2L));
        assertThat(result.getActiveCount(), is(2L));
        assertThat(result.getCompletedCount(), is(0L));

        OrderStageDTO firstStage = result.getStages().get(0);
        assertThat(firstStage.getId(), is(1L));
        assertThat(firstStage.getStageType(), is("FOUNDATION"));
        assertThat(firstStage.getProgress(), is(25));

        verify(orderRepository, times(1)).existsById(orderId);
        verify(orderStageRepository, times(1)).findByOrderId(orderId, PageRequest.of(0, 10));
        verify(orderStageRepository, times(1)).countActiveStages(orderId);
        verify(orderStageRepository, times(1)).countCompletedStages(orderId);
    }

    @Test
    void getOrderStages_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.existsById(orderId)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStageService.getOrderStages(orderId, PageRequest.of(0, 10))
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).existsById(orderId);
        verify(orderStageRepository, never()).findByOrderId(any(), any());
    }

    @Test
    void createOrderStage_ShouldCreateNewStageSuccessfully() {
        // Arrange
        OrderStage newStage = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .changedBy(testUser)
                .notes("Начата закладка фундамента")
                .progress(25)
                .isCompleted(false)
                .startDate(LocalDateTime.now())
                .plannedEndDate(createRequest.getPlannedEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        OrderStageDTO expectedDto = new OrderStageDTO();
        expectedDto.setId(stageId);
        expectedDto.setOrderId(orderId);
        expectedDto.setStageType("FOUNDATION");
        expectedDto.setDescription("Начата закладка фундамента");
        expectedDto.setProgress(25);
        expectedDto.setStatus("in_progress");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStageTypeRepository.findByName("FOUNDATION")).thenReturn(Optional.of(foundationStageType));
        when(orderStageRepository.findByOrderIdAndTypeName(orderId, "FOUNDATION")).thenReturn(List.of());
        when(orderStageRepository.save(any(OrderStage.class))).thenReturn(newStage);
        when(orderMapper.toStageDTO(newStage)).thenReturn(expectedDto);

        // Act
        OrderStageDTO result = orderStageService.createOrderStage(orderId, userId, createRequest);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(stageId));
        assertThat(result.getStageType(), is("FOUNDATION"));
        assertThat(result.getProgress(), is(25));

        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStageTypeRepository, times(1)).findByName("FOUNDATION");
        verify(orderStageRepository, times(1)).findByOrderIdAndTypeName(orderId, "FOUNDATION");
        verify(orderStageRepository, times(1)).save(any(OrderStage.class));
        verify(orderMapper, times(1)).toStageDTO(newStage);
    }

    @Test
    void createOrderStage_ShouldReturnExistingStage_WhenStageTypeAlreadyExists() {
        // Arrange
        OrderStage existingStage = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .notes("Начата закладка фундамента")
                .progress(30)
                .isCompleted(false)
                .startDate(LocalDateTime.now().minusDays(3))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        OrderStageDTO existingDto = new OrderStageDTO();
        existingDto.setId(stageId);
        existingDto.setOrderId(orderId);
        existingDto.setStageType("FOUNDATION");
        existingDto.setDescription("Начата закладка фундамента");
        existingDto.setProgress(30);
        existingDto.setStatus("in_progress");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStageTypeRepository.findByName("FOUNDATION")).thenReturn(Optional.of(foundationStageType));
        when(orderStageRepository.findByOrderIdAndTypeName(orderId, "FOUNDATION")).thenReturn(List.of(existingStage));
        when(orderMapper.toStageDTO(existingStage)).thenReturn(existingDto);

        // Act
        OrderStageDTO result = orderStageService.createOrderStage(orderId, userId, createRequest);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(stageId));
        assertThat(result.getStageType(), is("FOUNDATION"));

        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStageTypeRepository, times(1)).findByName("FOUNDATION");
        verify(orderStageRepository, times(1)).findByOrderIdAndTypeName(orderId, "FOUNDATION");
        verify(orderStageRepository, never()).save(any(OrderStage.class));
        verify(orderMapper, times(1)).toStageDTO(existingStage);
    }

    @Test
    void createOrderStage_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStageService.createOrderStage(orderId, userId, createRequest)
        );

        assertThat(exception.getMessage(), containsString("Заказ не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void createOrderStage_ShouldThrowException_WhenStageTypeNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStageTypeRepository.findByName("FOUNDATION")).thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> orderStageService.createOrderStage(orderId, userId, createRequest)
        );

        assertThat(exception.getMessage(), containsString("Тип этапа не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStageTypeRepository, times(1)).findByName("FOUNDATION");
    }

    @Test
    void updateOrderStage_ShouldUpdateStageSuccessfully() {
        // Arrange
        OrderStage existingStage = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .notes("Начата закладка фундамента")
                .progress(25)
                .isCompleted(false)
                .startDate(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        OrderStage updatedStage = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .notes("Начата закладка фундамента")
                .progress(50)
                .isCompleted(true)
                .startDate(LocalDateTime.now().minusDays(5))
                .completionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        OrderStageDTO expectedDto = new OrderStageDTO();
        expectedDto.setId(stageId);
        expectedDto.setOrderId(orderId);
        expectedDto.setStageType("FOUNDATION");
        expectedDto.setDescription("Начата закладка фундамента");
        expectedDto.setProgress(50);
        expectedDto.setStatus("completed");

        when(orderStageRepository.findById(stageId)).thenReturn(Optional.of(existingStage));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderStageRepository.save(any(OrderStage.class))).thenReturn(updatedStage);
        when(orderMapper.toStageDTO(updatedStage)).thenReturn(expectedDto);

        // Act
        OrderStageDTO result = orderStageService.updateOrderStage(stageId, userId, updateRequest);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getProgress(), is(50));
        assertThat(result.getStatus(), is("completed"));

        verify(orderStageRepository, times(1)).findById(stageId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStageRepository, times(1)).save(any(OrderStage.class));
        verify(orderMapper, times(1)).toStageDTO(updatedStage);
    }

    @Test
    void updateOrderStage_ShouldThrowException_WhenStageNotFound() {
        // Arrange
        when(orderStageRepository.findById(stageId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStageService.updateOrderStage(stageId, userId, updateRequest)
        );

        assertThat(exception.getMessage(), containsString("Этап не найден"));
        verify(orderStageRepository, times(1)).findById(stageId);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateOrderStage_ShouldThrowException_WhenProgressOutOfRange() {
        // Arrange
        OrderStage existingStage = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .notes("Начата закладка фундамента")
                .progress(25)
                .isCompleted(false)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        updateRequest.setProgress(150); // Неправильное значение

        when(orderStageRepository.findById(stageId)).thenReturn(Optional.of(existingStage));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> orderStageService.updateOrderStage(stageId, userId, updateRequest)
        );

        assertThat(exception.getMessage(), containsString("Прогресс должен быть в диапазоне 0-100"));
        verify(orderStageRepository, times(1)).findById(stageId);
        verify(userRepository, times(1)).findById(userId);
        verify(orderStageRepository, never()).save(any());
    }

    @Test
    void getCurrentStage_ShouldReturnCurrentStage() {
        // Arrange
        OrderStage currentStage = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .notes("Начата закладка фундамента")
                .progress(25)
                .isCompleted(false)
                .startDate(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        OrderStageDTO expectedDto = new OrderStageDTO();
        expectedDto.setId(stageId);
        expectedDto.setOrderId(orderId);
        expectedDto.setStageType("FOUNDATION");
        expectedDto.setDescription("Начата закладка фундамента");
        expectedDto.setProgress(25);
        expectedDto.setStatus("in_progress");

        when(orderStageRepository.findCurrentStageByOrderId(orderId)).thenReturn(Optional.of(currentStage));
        when(orderMapper.toStageDTO(currentStage)).thenReturn(expectedDto);

        // Act
        OrderStageDTO result = orderStageService.getCurrentStage(orderId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(stageId));
        assertThat(result.getStageType(), is("FOUNDATION"));
        assertThat(result.getProgress(), is(25));

        verify(orderStageRepository, times(1)).findCurrentStageByOrderId(orderId);
        verify(orderMapper, times(1)).toStageDTO(currentStage);
    }

    @Test
    void getCurrentStage_ShouldThrowException_WhenNoCurrentStage() {
        // Arrange
        when(orderStageRepository.findCurrentStageByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStageService.getCurrentStage(orderId)
        );

        assertThat(exception.getMessage(), containsString("Активный этап не найден"));
        verify(orderStageRepository, times(1)).findCurrentStageByOrderId(orderId);
        verify(orderMapper, never()).toStageDTO(any());
    }

    @Test
    void deleteCurrentStage_ShouldDeleteStageSuccessfully() {
        // Arrange
        OrderStage stageToDelete = OrderStage.builder()
                .id(stageId)
                .order(testOrder)
                .type(foundationStageType)
                .build();

        when(orderStageRepository.findById(stageId)).thenReturn(Optional.of(stageToDelete));
        doNothing().when(orderStageRepository).delete(stageToDelete);

        // Act
        orderStageService.deleteCurrentStage(stageId);

        // Assert
        verify(orderStageRepository, times(1)).findById(stageId);
        verify(orderStageRepository, times(1)).delete(stageToDelete);
    }

    @Test
    void deleteCurrentStage_ShouldThrowException_WhenStageNotFound() {
        // Arrange
        when(orderStageRepository.findById(stageId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> orderStageService.deleteCurrentStage(stageId)
        );

        assertThat(exception.getMessage(), containsString("Этап не найден"));
        verify(orderStageRepository, times(1)).findById(stageId);
        verify(orderStageRepository, never()).delete(any());
    }
}