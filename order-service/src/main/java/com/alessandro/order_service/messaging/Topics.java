package com.alessandro.order_service.messaging;

public final class Topics {
    public static final String SHIPPING_COMMANDS = "shipping.commands";
    public static final String SHIPPING_REPLIES = "shipping.replies";
    public static final String SAGA_EVENTS = "saga.events";

    private Topics() {}
}