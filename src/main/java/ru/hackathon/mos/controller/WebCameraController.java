package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.mos.dto.webcamera.WebCameraRequest;
import ru.hackathon.mos.dto.webcamera.WebCameraResponse;
import ru.hackathon.mos.service.OrderService;
import ru.hackathon.mos.service.WebCameraService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/webCameras")
@RequiredArgsConstructor
@Tag(name = "Веб-камеры", description = "Управление видеонаблюдением на стройплощадке")
public class WebCameraController {
    private final OrderService orderService;
    private final WebCameraService webCameraService;

    @PostMapping
    @Operation(summary = "Добавить веб-камеру в проект")
    public ResponseEntity<WebCameraResponse> addWebCamera(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @Valid @RequestBody WebCameraRequest webCameraRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        WebCameraResponse response = webCameraService.addWebCamera(orderId, webCameraRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{cameraId}")
    @Operation(summary = "Удалить камеру")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteWebCamera(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @PathVariable Long webCameraId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        webCameraService.deleteWebCamera(orderId, webCameraId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Получить список всех камер на стройплощадке")
    public ResponseEntity<List<WebCameraResponse>> getWebCameras(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        List<WebCameraResponse> cameras = webCameraService.getWebCameras(orderId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/{cameraId}")
    @Operation(summary = "Получить информацию о конкретной камере")
    public ResponseEntity<WebCameraResponse> getWebCamera(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @PathVariable Long webCameraId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        WebCameraResponse response = webCameraService.getWebCamera(orderId, webCameraId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cameraId}")
    @Operation(summary = "Обновить информацию о камере")
    public ResponseEntity<WebCameraResponse> updateWebCamera(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @PathVariable Long webCameraId,
            @Valid @RequestBody WebCameraRequest webCameraRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        WebCameraResponse response = webCameraService.updateWebCamera(orderId, webCameraId, webCameraRequest);
        return ResponseEntity.ok(response);
    }
}