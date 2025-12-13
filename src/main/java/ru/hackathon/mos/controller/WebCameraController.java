package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.mos.dto.webcamera.WebCameraRequest;
import ru.hackathon.mos.dto.webcamera.WebCameraResponse;
import ru.hackathon.mos.service.WebCameraService;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/webCameras")
@RequiredArgsConstructor
@Tag(name = "Веб-камеры", description = "Управление видеонаблюдением на стройплощадке")
public class WebCameraController {
    private final WebCameraService webCameraService;

    @PostMapping
    @Operation(summary = "Добавить веб-камеру в проект")
    public ResponseEntity<WebCameraResponse> addWebCamera(
            @PathVariable Long orderId,
            @Valid @RequestBody WebCameraRequest request) {

        WebCameraResponse response = webCameraService.addWebCamera(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{cameraId}")
    @Operation(summary = "Удалить камеру")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteWebCamera(
            @PathVariable Long orderId,
            @PathVariable Long cameraId) {

        webCameraService.deleteWebCamera(orderId, cameraId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Получить список всех камер на стройплощадке")
    public ResponseEntity<List<WebCameraResponse>> getWebCameras(
            @PathVariable Long orderId) {

        List<WebCameraResponse> cameras = webCameraService.getWebCameras(orderId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/{cameraId}")
    @Operation(summary = "Получить информацию о конкретной камере")
    public ResponseEntity<WebCameraResponse> getWebCamera(
            @PathVariable Long orderId,
            @PathVariable Long cameraId) {

        WebCameraResponse response = webCameraService.getWebCamera(orderId, cameraId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cameraId}")
    @Operation(summary = "Обновить информацию о камере")
    public ResponseEntity<WebCameraResponse> updateWebCamera(
            @PathVariable Long orderId,
            @PathVariable Long cameraId,
            @Valid @RequestBody WebCameraRequest request) {

        WebCameraResponse response = webCameraService.updateWebCamera(orderId, cameraId, request);
        return ResponseEntity.ok(response);
    }
}