package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.common.ListResponse;
import ru.hackathon.mos.dto.order.CreateOrderRequest;
import ru.hackathon.mos.dto.order.OrderDTO;
import ru.hackathon.mos.dto.order.OrderStageDTO;
import ru.hackathon.mos.dto.order.OrderStatusDTO;
import ru.hackathon.mos.service.OrderService;
import ru.hackathon.mos.service.OrderStageService;
import ru.hackathon.mos.service.OrderStatusService;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы", description = "Управление заказами строительства")
public class OrderController {

    private final OrderService orderService;
    private final OrderStageService orderStageService;
    private final OrderStatusService orderStatusService;

    @GetMapping
    @Operation(summary = "Список заказов текущего пользователя",
            description = "Получить список заказов авторизованного пользователя")
    public ResponseEntity<ListResponse<OrderDTO>> getCurrentUserOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        UUID userId = UUID.fromString(jwt.getSubject());

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equals(sortParams[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        ListResponse<OrderDTO> orders = orderService.getOrdersByUser(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Информация о заказе", description = "Получить информацию о заказе")
    public ResponseEntity<OrderDTO> getOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/stages")
    @Operation(summary = "Этапы строительства",
            description = "Получить историю этапов строительства по заказу")
    public ResponseEntity<OrderStageDTO.StageListResponse> getOrderStages(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        OrderStageDTO.StageListResponse stages = orderStageService.getOrderStages(orderId, pageable);
        return ResponseEntity.ok(stages);
    }

    @PostMapping("/{orderId}/stages")
    @Operation(summary = "Создать этап строительства",
            description = "Создать новый этап строительства")
    public ResponseEntity<OrderStageDTO> createOrderStage(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @Valid @RequestBody OrderStageDTO.CreateStageRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        OrderStageDTO stage = orderStageService.createOrderStage(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stage);
    }

    @GetMapping("/{orderId}/status")
    @Operation(summary = "Статусы заказа", description = "Получить все статусы заказа")
    public ResponseEntity<OrderStatusDTO.StatusListResponse> getAllOrderStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        OrderStatusDTO.StatusListResponse statuses = orderStatusService.getAllOrderStatuses(orderId, pageable);
        return ResponseEntity.ok(statuses);
    }

    @PostMapping("/{orderId}/status")
    @Operation(summary = "Добавить статус заказа", description = "Добавить новый статус в заказ")
    public ResponseEntity<OrderStatusDTO> createOrderStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusDTO.CreateStatusRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        OrderStatusDTO status = orderStatusService.createOrderStatus(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PostMapping
    @Operation(summary = "Создать заказ", description = "Создать новый заказ")
    public ResponseEntity<OrderDTO> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PatchMapping("/{orderId}/stages/{stageId}")
    @Operation(summary = "Обновить этап", description = "Обновить информацию об этапе")
    public ResponseEntity<OrderStageDTO> updateOrderStage(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @Parameter(description = "ID этапа") @PathVariable Long stageId,
            @Valid @RequestBody OrderStageDTO.UpdateStageRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        OrderStageDTO stage = orderStageService.updateOrderStage(stageId, userId, request);
        return ResponseEntity.ok(stage);
    }

    @GetMapping("/{orderId}/stages/current")
    @Operation(summary = "Текущий этап", description = "Получить текущий активный этап заказа")
    public ResponseEntity<OrderStageDTO> getCurrentStage(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        OrderStageDTO stage = orderStageService.getCurrentStage(orderId);
        return ResponseEntity.ok(stage);
    }

    @GetMapping("/{orderId}/status/current")
    @Operation(summary = "Текущий статус", description = "Получить текущий статус заказа")
    public ResponseEntity<OrderStatusDTO> getCurrentStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID заказа") @PathVariable Long orderId) {

        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        OrderStatusDTO status = orderStatusService.getCurrentStatus(orderId);
        return ResponseEntity.ok(status);
    }
}