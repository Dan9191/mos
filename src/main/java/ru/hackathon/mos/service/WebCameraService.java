package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.webcamera.WebCameraRequest;
import ru.hackathon.mos.dto.webcamera.WebCameraResponse;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.WebCamera;
import ru.hackathon.mos.exception.OrderNotFoundException;
import ru.hackathon.mos.exception.WebCameraNotFoundException;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.WebCameraRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WebCameraService {

    private final WebCameraRepository webCameraRepository;
    private final OrderRepository orderRepository;

    /**
     * Добавить веб-камеру в проект
     */
    @Transactional
    public WebCameraResponse addWebCamera(Long orderId, WebCameraRequest request) {
        log.info("Добавление веб-камеры для заказа с ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Конвертируем порт из строки в число
        Integer port = null;
        if (request.port() != null && !request.port().isEmpty()) {
            try {
                port = Integer.parseInt(request.port());
                // Валидация порта
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException("Порт должен быть в диапазоне 1-65535");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный формат порта");
            }
        }

        WebCamera webCamera = WebCamera.builder()
                .order(order)
                .name(request.name())
                .ipAddress(request.ipAddress())
                .port(port)
                .build();

        WebCamera savedCamera = webCameraRepository.save(webCamera);
        log.info("Веб-камера добавлена с ID: {}", savedCamera.getId());

        return convertToResponse(savedCamera);
    }

    /**
     * Получить список камер на стройплощадке
     */
    public List<WebCameraResponse> getWebCameras(Long orderId) {
        log.info("Получение списка веб-камер для заказа с ID: {}", orderId);

        // Проверяем существование заказа
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }

        List<WebCamera> cameras = webCameraRepository.findByOrderId(orderId);

        return cameras.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Получить информацию о конкретной камере
     */
    public WebCameraResponse getWebCamera(Long orderId, Long cameraId) {
        log.info("Получение веб-камеры с ID: {} для заказа с ID: {}", cameraId, orderId);

        WebCamera webCamera = webCameraRepository.findByIdAndOrderId(cameraId, orderId)
                .orElseThrow(() -> new WebCameraNotFoundException("Камера не найдена"));

        return convertToResponse(webCamera);
    }

    /**
     * Обновить информацию о камере
     */
    @Transactional
    public WebCameraResponse updateWebCamera(Long orderId, Long cameraId, WebCameraRequest request) {
        log.info("Обновление веб-камеры с ID: {} для заказа с ID: {}", cameraId, orderId);

        WebCamera webCamera = webCameraRepository.findByIdAndOrderId(cameraId, orderId)
                .orElseThrow(() -> new WebCameraNotFoundException("Камера не найдена"));

        // Обновляем поля
        webCamera.setName(request.name());
        webCamera.setIpAddress(request.ipAddress());

        if (request.port() != null && !request.port().isEmpty()) {
            try {
                Integer port = Integer.parseInt(request.port());
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException("Порт должен быть в диапазоне 1-65535");
                }
                webCamera.setPort(port);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный формат порта");
            }
        } else {
            webCamera.setPort(null);
        }

        WebCamera updatedCamera = webCameraRepository.save(webCamera);
        log.info("Веб-камера с ID {} обновлена", cameraId);

        return convertToResponse(updatedCamera);
    }

    /**
     * Удалить камеру
     */
    @Transactional
    public void deleteWebCamera(Long orderId, Long cameraId) {
        log.info("Удаление веб-камеры с ID: {} для заказа с ID: {}", cameraId, orderId);

        WebCamera webCamera = webCameraRepository.findByIdAndOrderId(cameraId, orderId)
                .orElseThrow(() -> new WebCameraNotFoundException("Камера не найдена"));

        webCameraRepository.delete(webCamera);
        log.info("Веб-камера с ID {} удалена", cameraId);
    }

    /**
     * Получить URL для трансляции
     */
    public String getStreamUrl(Long orderId, Long cameraId) {
        WebCamera webCamera = webCameraRepository.findByIdAndOrderId(cameraId, orderId)
                .orElseThrow(() -> new WebCameraNotFoundException("Камера не найдена"));

        return generateStreamUrl(webCamera);
    }

    /**
     * Конвертация WebCamera в WebCameraResponse
     */
    private WebCameraResponse convertToResponse(WebCamera webCamera) {
        return WebCameraResponse.builder()
                .id(webCamera.getId())
                .name(webCamera.getName())
                .ipAddress(webCamera.getIpAddress())
                .port(webCamera.getPort())
                .streamUrl(generateStreamUrl(webCamera))
                .build();
    }

    /**
     * Генерация URL для трансляции
     */
    private String generateStreamUrl(WebCamera webCamera) {
        if (webCamera.getIpAddress() == null || webCamera.getPort() == null) {
            return null;
        }

        // Пример: rtsp://192.168.1.100:554/stream
        return String.format("rtsp://%s:%d/stream",
                webCamera.getIpAddress(),
                webCamera.getPort());
    }
}