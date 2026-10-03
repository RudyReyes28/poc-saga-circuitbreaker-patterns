package com.alessandro.order_service.client;


import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.HttpClientErrorException;

/** Resultado de llamar a otro servicio, sin lanzar excepciones para fallos de negocio. */
public record StepResult<T>(Outcome outcome, T data, String message) {

    public enum Outcome { SUCCESS, BUSINESS_FAILURE, UNAVAILABLE }

    public static <T> StepResult<T> success(T data) {
        return new StepResult<>(Outcome.SUCCESS, data, null);
    }

    /** 4xx: el servicio respondió correctamente que "no". NO cuenta como fallo del circuito. */
    public static <T> StepResult<T> fromHttpError(HttpClientErrorException ex) {
        return new StepResult<>(Outcome.BUSINESS_FAILURE, null,
                "HTTP " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString());
    }

    /** Fallback: servicio caído, lento o circuito abierto. */
    public static <T> StepResult<T> fromFailure(String service, Throwable throwable) {
        String detail = (throwable instanceof CallNotPermittedException) ? " (circuito abierto)" : "";
        return new StepResult<>(Outcome.UNAVAILABLE, null,
                "Servicio de " + service + " no disponible" + detail + ", intente más tarde");
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }
}