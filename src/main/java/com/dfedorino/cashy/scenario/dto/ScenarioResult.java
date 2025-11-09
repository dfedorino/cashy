package com.dfedorino.cashy.scenario.dto;

public record ScenarioResult<T>(
        Throwable failureReason,
        T result
) {

    public static <T> ScenarioResult<T> failure(Throwable error) {
        return new ScenarioResult<>(error, null);
    }

    public static <T> ScenarioResult<T> success(T result) {
        return new ScenarioResult<>(null, result);
    }

    public boolean isSuccess() {
        return failureReason == null;
    }
}
