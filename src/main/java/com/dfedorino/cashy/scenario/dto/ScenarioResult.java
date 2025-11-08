package com.dfedorino.cashy.scenario.dto;

import java.util.Optional;

public record ScenarioResult<T>(
        Throwable failureReason,
        Optional<T> result
) {

    public static <T> ScenarioResult<T> failure(Throwable error) {
        return new ScenarioResult<>(error, Optional.empty());
    }

    public static <T> ScenarioResult<T> success(T result) {
        return new ScenarioResult<>(null, Optional.ofNullable(result));
    }

    public boolean isSuccess() {
        return failureReason == null;
    }
}
