package ru.hackathon.mos.exception.order;

import ru.hackathon.mos.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(Long orderId) {
        super("Заказ", orderId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}