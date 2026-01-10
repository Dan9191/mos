package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.hackathon.mos.dto.webcamera.WebCameraRequest;
import ru.hackathon.mos.dto.webcamera.WebCameraResponse;
import ru.hackathon.mos.service.WebCameraService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WebCameraControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WebCameraService webCameraService;

    @InjectMocks
    private WebCameraController webCameraController;

    private ObjectMapper objectMapper;
    private Long orderId;
    private WebCameraResponse testCamera;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        orderId = 1L;

        testCamera = WebCameraResponse.builder()
                .id(1L)
                .name("Камера на входе")
                .ipAddress("192.168.1.100")
                .port(554)
                .streamUrl("https://rtsp.me/embed/abcd123")
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(webCameraController).build();
    }

    @Test
    void getWebCamera_ShouldReturnCamera() throws Exception {
        /// Arrange
        Long cameraId = 1L;
        when(webCameraService.getWebCamera(orderId, cameraId)).thenReturn(testCamera);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/webCameras/{cameraId}", orderId, cameraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Камера на входе")))
                .andExpect(jsonPath("$.ip", is("192.168.1.100")))
                .andExpect(jsonPath("$.port", is(554)))
                .andExpect(jsonPath("$.streamUrl", is("https://rtsp.me/embed/abcd123")));
    }

    @Test
    void getWebCameras_ShouldReturnListOfCameras() throws Exception {
        // Arrange
        WebCameraResponse camera2 = WebCameraResponse.builder()
                .id(2L)
                .name("Камера на стройке")
                .ipAddress("192.168.1.101")
                .port(555)
                .streamUrl("https://rtsp.me/embed/efgh456")
                .build();

        List<WebCameraResponse> cameras = List.of(testCamera, camera2);

        when(webCameraService.getWebCameras(orderId)).thenReturn(cameras);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}/webCameras", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Камера на входе")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Камера на стройке")));
    }

    @Test
    void addWebCamera_ShouldCreateCamera() throws Exception {
        // Arrange
        WebCameraRequest request = new WebCameraRequest(
                "Новая камера",
                "192.168.1.200",
                "8080"
        );

        WebCameraResponse createdCamera = WebCameraResponse.builder()
                .id(3L)
                .name("Новая камера")
                .ipAddress("192.168.1.200")
                .port(8080)
                .streamUrl("https://rtsp.me/embed/xyz789")
                .build();

        when(webCameraService.addWebCamera(eq(orderId), any(WebCameraRequest.class)))
                .thenReturn(createdCamera);

        // Act & Assert
        mockMvc.perform(post("/api/orders/{orderId}/webCameras", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Новая камера")))
                .andExpect(jsonPath("$.ip", is("192.168.1.200")))
                .andExpect(jsonPath("$.port", is(8080)));
    }

    @Test
    void deleteWebCamera_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        Long cameraId = 1L;
        doNothing().when(webCameraService).deleteWebCamera(orderId, cameraId);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/{orderId}/webCameras/{cameraId}", orderId, cameraId))
                .andExpect(status().isNoContent());
    }
}