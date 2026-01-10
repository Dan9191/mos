package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hackathon.mos.dto.webcamera.WebCameraRequest;
import ru.hackathon.mos.dto.webcamera.WebCameraResponse;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.WebCamera;
import ru.hackathon.mos.exception.OrderNotFoundException;
import ru.hackathon.mos.exception.WebCameraNotFoundException;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.WebCameraRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCameraServiceTest {

    @Mock
    private WebCameraRepository webCameraRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private WebCameraService webCameraService;

    private Order testOrder;
    private WebCamera testCamera;
    private Long orderId;
    private Long cameraId;

    @BeforeEach
    void setUp() {
        orderId = 1L;
        cameraId = 1L;

        testOrder = Order.builder()
                .id(orderId)
                .build();

        testCamera = WebCamera.builder()
                .id(cameraId)
                .order(testOrder)
                .name("Камера на входе")
                .ipAddress("192.168.1.100")
                .port(554)
                .build();
    }

    @Test
    void addWebCamera_ShouldAddCameraSuccessfully() {
        // Arrange
        WebCameraRequest request = new WebCameraRequest(
                "Камера на стройке",
                "192.168.1.200",
                "8080"
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(webCameraRepository.save(any(WebCamera.class))).thenReturn(testCamera);

        // Act
        WebCameraResponse response = webCameraService.addWebCamera(orderId, request);

        // Assert
        assertThat(response, is(notNullValue()));
        assertThat(response.id(), is(cameraId));
        assertThat(response.name(), is("Камера на входе"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(webCameraRepository, times(1)).save(any(WebCamera.class));
    }

    @Test
    void addWebCamera_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        WebCameraRequest request = new WebCameraRequest(
                "Камера на стройке",
                "192.168.1.200",
                "8080"
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> webCameraService.addWebCamera(orderId, request)
        );

        assertThat(exception.getMessage(), containsString(String.valueOf(orderId)));
        verify(orderRepository, times(1)).findById(orderId);
        verify(webCameraRepository, never()).save(any());
    }

    @Test
    void getWebCamera_ShouldReturnCamera_WhenCameraExists() {
        // Arrange
        when(webCameraRepository.findByIdAndOrderId(cameraId, orderId))
                .thenReturn(Optional.of(testCamera));

        // Act
        WebCameraResponse response = webCameraService.getWebCamera(orderId, cameraId);

        // Assert
        assertThat(response, is(notNullValue()));
        assertThat(response.id(), is(cameraId));
        assertThat(response.name(), is("Камера на входе"));
        assertThat(response.ipAddress(), is("192.168.1.100"));
        assertThat(response.port(), is(554));
        verify(webCameraRepository, times(1)).findByIdAndOrderId(cameraId, orderId);
    }

    @Test
    void getWebCamera_ShouldThrowException_WhenCameraNotFound() {
        // Arrange
        when(webCameraRepository.findByIdAndOrderId(cameraId, orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        WebCameraNotFoundException exception = assertThrows(
                WebCameraNotFoundException.class,
                () -> webCameraService.getWebCamera(orderId, cameraId)
        );

        assertThat(exception.getMessage(), containsString("Камера не найдена"));
        verify(webCameraRepository, times(1)).findByIdAndOrderId(cameraId, orderId);
    }

    @Test
    void getWebCameras_ShouldReturnListOfCameras() {
        // Arrange
        WebCamera camera2 = WebCamera.builder()
                .id(2L)
                .order(testOrder)
                .name("Камера на стройке")
                .ipAddress("192.168.1.101")
                .port(555)
                .build();

        List<WebCamera> cameras = List.of(testCamera, camera2);

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(webCameraRepository.findByOrderId(orderId)).thenReturn(cameras);

        // Act
        List<WebCameraResponse> responses = webCameraService.getWebCameras(orderId);

        // Assert
        assertThat(responses, hasSize(2));
        assertThat(responses.get(0).id(), is(1L));
        assertThat(responses.get(0).name(), is("Камера на входе"));
        assertThat(responses.get(1).id(), is(2L));
        assertThat(responses.get(1).name(), is("Камера на стройке"));
        verify(orderRepository, times(1)).existsById(orderId);
        verify(webCameraRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void getWebCameras_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.existsById(orderId)).thenReturn(false);

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> webCameraService.getWebCameras(orderId)
        );

        assertThat(exception.getMessage(), containsString(String.valueOf(orderId)));
        verify(orderRepository, times(1)).existsById(orderId);
        verify(webCameraRepository, never()).findByOrderId(any());
    }

    @Test
    void deleteWebCamera_ShouldDeleteCameraSuccessfully() {
        // Arrange
        when(webCameraRepository.findByIdAndOrderId(cameraId, orderId))
                .thenReturn(Optional.of(testCamera));
        doNothing().when(webCameraRepository).delete(testCamera);

        // Act
        webCameraService.deleteWebCamera(orderId, cameraId);

        // Assert
        verify(webCameraRepository, times(1)).findByIdAndOrderId(cameraId, orderId);
        verify(webCameraRepository, times(1)).delete(testCamera);
    }

    @Test
    void deleteWebCamera_ShouldThrowException_WhenCameraNotFound() {
        // Arrange
        when(webCameraRepository.findByIdAndOrderId(cameraId, orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        WebCameraNotFoundException exception = assertThrows(
                WebCameraNotFoundException.class,
                () -> webCameraService.deleteWebCamera(orderId, cameraId)
        );

        assertThat(exception.getMessage(), containsString("Камера не найдена"));
        verify(webCameraRepository, times(1)).findByIdAndOrderId(cameraId, orderId);
        verify(webCameraRepository, never()).delete(any());
    }
}