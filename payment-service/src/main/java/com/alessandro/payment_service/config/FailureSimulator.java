package com.alessandro.payment_service.config;

import com.alessandro.payment_service.exception.SimulatedFailureException;
import org.springframework.stereotype.Component;

@Component
public class FailureSimulator {

    public enum Mode { NORMAL, ERROR, SLOW }

    private volatile Mode mode = Mode.NORMAL;
    private volatile long delayMs = 3000;

    /** Se invoca al inicio del endpoint que se quiere sabotear. */
    public void apply() {
        switch (mode) {
            case ERROR -> throw new SimulatedFailureException("Fallo simulado en Inventory Service");
            case SLOW -> {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            default -> { }
        }
    }

    public void configure(Mode mode, Long delayMs) {
        this.mode = mode;
        if (delayMs != null) {
            this.delayMs = delayMs;
        }
    }

    public Mode getMode() { return mode; }

    public long getDelayMs() { return delayMs; }
}