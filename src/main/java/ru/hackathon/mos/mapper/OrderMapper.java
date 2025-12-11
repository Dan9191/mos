package ru.hackathon.mos.mapper;

import org.springframework.stereotype.Component;
import ru.hackathon.mos.dto.order.OrderDTO;
import ru.hackathon.mos.dto.order.*;
import ru.hackathon.mos.entity.*;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        if (order == null) return null;

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setAddress(order.getAddress());
        orderDTO.setCreatedAt(order.getCreatedAt());

        // Маппинг клиента
        if (order.getClient() != null) {
            OrderDTO.UserInfoDTO userInfo = new OrderDTO.UserInfoDTO();
            userInfo.setId(order.getClient().getId());
            userInfo.setFullName(formatFullName(
                    order.getClient().getLastName(),
                    order.getClient().getFirstName(),
                    order.getClient().getMiddleName()
            ));
            userInfo.setEmail(order.getClient().getEmail());
            orderDTO.setClientInfo(userInfo);
        }

        // Маппинг проекта
        if (order.getProject() != null) {
            OrderDTO.ProjectInfoDTO projectInfo = new OrderDTO.ProjectInfoDTO();
            projectInfo.setId(order.getProject().getId());
            projectInfo.setTitle(order.getProject().getTitle());
            projectInfo.setBasePrice(order.getProject().getBasePrice() != null ?
                    order.getProject().getBasePrice().toString() : "0");
            projectInfo.setTotalArea(order.getProject().getAreaM2() != null ?
                    order.getProject().getAreaM2().toString() : "0");
            orderDTO.setProjectInfo(projectInfo);
        }

        return orderDTO;
    }

    public OrderStatusDTO toStatusDTO(OrderStatus orderStatus) {
        if (orderStatus == null) return null;

        OrderStatusDTO dto = new OrderStatusDTO();
        dto.setId(orderStatus.getId());
        dto.setOrderId(orderStatus.getOrder() != null ? orderStatus.getOrder().getId() : null);
        dto.setStatusType(orderStatus.getType() != null ? orderStatus.getType().getName().toString() : null);
        dto.setComment(null);
        dto.setChangedBy(toChangedByUser(orderStatus.getChangedBy()));
        dto.setCreatedAt(orderStatus.getCreatedAt());

        return dto;
    }

    public OrderStageDTO toStageDTO(OrderStage orderStage) {
        if (orderStage == null) return null;

        OrderStageDTO dto = new OrderStageDTO();
        dto.setId(orderStage.getId());
        dto.setOrderId(orderStage.getOrder() != null ? orderStage.getOrder().getId() : null);
        dto.setStageType(orderStage.getType() != null ? orderStage.getType().getName().toString() : null);
        dto.setStageName(orderStage.getType() != null ? orderStage.getType().getDescription() : null);
        dto.setDescription(orderStage.getNotes());
        dto.setProgress(orderStage.getProgress());
        dto.setStatus(toStageStatus(orderStage.getIsCompleted()));
        dto.setCreatedBy(toChangedByUser(orderStage.getChangedBy()));
        dto.setCreatedAt(orderStage.getCreatedAt());
        dto.setActualEndDate(orderStage.getCompletionDate());
        dto.setStartDate(orderStage.getStartDate());
        dto.setPlannedEndDate(orderStage.getPlannedEndDate());

        return dto;
    }

    private ChangedByUserDTO toChangedByUser(User user) {
        if (user == null) return null;

        ChangedByUserDTO dto = new ChangedByUserDTO();
        dto.setId(user.getId());
        dto.setFullName(formatFullName(user.getLastName(), user.getFirstName(), user.getMiddleName()));
        dto.setRole(user.getType() != null ? user.getType().getName() : "Пользователь");

        return dto;
    }

    private String toStageStatus(Boolean isCompleted) {
        if (isCompleted == null) return "unknown";
        return isCompleted ? "completed" : "in_progress";
    }

    private String formatFullName(String lastName, String firstName, String middleName) {
        StringBuilder fullName = new StringBuilder();
        if (lastName != null) fullName.append(lastName);
        if (firstName != null) fullName.append(" ").append(firstName);
        if (middleName != null) fullName.append(" ").append(middleName);
        return fullName.toString().trim();
    }
}