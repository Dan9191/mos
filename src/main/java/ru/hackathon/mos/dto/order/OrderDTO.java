package ru.hackathon.mos.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private UserInfoDTO clientInfo;
    private ProjectInfoDTO projectInfo;
    private String address;
    private OrderStatusDTO currentStatus;
    private OrderStageDTO currentStage;
    private LocalDateTime createdAt;

    //данные менеджера
    private UUID managerId;
    private String managerFullName;
    private String managerContact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private UUID id;
        private String fullName;
        private String email;
        private String contact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfoDTO {
        private Long id;
        private String title;
        private String basePrice;
        private String totalArea;
    }
}