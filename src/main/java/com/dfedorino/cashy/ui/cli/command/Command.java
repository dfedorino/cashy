package com.dfedorino.cashy.ui.cli.command;

import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a CLI command that can be executed with arguments.
 *
 * <p>Provides common validation helpers for argument checking, limits, URLs, UUIDs,
 * and short links common for all commands. Implementations of this interface should define the
 * command key, description, example usage, and the logic of applying the given arguments.
 *
 * @param <T> the type of the result returned by the command
 */
public interface Command<T> {

    String COMMAND_INVALID_MESSAGE = "Command invalid, example: '%s'";
    String AMOUNT_INVALID_MESSAGE = "Amount invalid: '%s'";
    String ALERT_THRESHOLD_INVALID_MESSAGE = "Alert threshold invalid, must be between 1 and 100, value: '%s'";

    /**
     * Executes the command with the given arguments.
     *
     * @param args the command arguments
     * @return the result of execution wrapped in {@link ResultWithNotification}
     */
    ResultWithNotification<T> apply(String... args);

    /**
     * Returns the unique key or name of the command.
     *
     * @return the command key
     */
    String key();

    /**
     * Returns a description of what the command does.
     *
     * @return the command description
     */
    String description();

    /**
     * Returns an example usage of the command.
     *
     * @return example usage
     */
    String example();

    /**
     * Validates command arguments using the given condition, typically for length check.
     *
     * @param condition      predicate to test the arguments
     * @param commandAndArgs the arguments to validate
     * @return an {@link Optional} containing an error result if validation fails, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateArguments(Predicate<String[]> condition,
                                                                  String[] commandAndArgs) {
        if (!condition.test(commandAndArgs)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.COMMAND_INVALID_MESSAGE.formatted(example())));
        }
        return Optional.empty();
    }

    /**
     * Validates amount.
     *
     * @param amount      string supposed to represent a valid amount
     * @return an {@link Optional} containing an error result if validation fails, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateAmount(String amount) {
        try {
            new BigDecimal(amount);
            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.AMOUNT_INVALID_MESSAGE.formatted(amount)));
        }
    }

    /**
     * Validates alert threshold.
     *
     * @param alertThreshold      string supposed to represent a valid limit (between 1 and 100)
     * @return an {@link Optional} containing an error result if validation fails, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateAlertThreshold(String alertThreshold) {
        try {
            int limitInt = Integer.parseInt(alertThreshold);
            if (limitInt < 1 || limitInt > 100) {
                throw new IllegalArgumentException();
            }
            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.ALERT_THRESHOLD_INVALID_MESSAGE.formatted(alertThreshold)));
        }
    }

    /**
     * Performs multiple validations in order and returns the first failing result, if any.
     *
     * @param validations a list of suppliers for individual validations
     * @return the first failing validation result, or empty if all validations pass
     */
    default Optional<ResultWithNotification<T>> validateInOrder(
            List<Supplier<Optional<ResultWithNotification<T>>>> validations) {

        for (var validation : validations) {
            Optional<ResultWithNotification<T>> failedValidation = validation.get();
            if (failedValidation.isPresent()) {
                return failedValidation;
            }
        }
        return Optional.empty();
    }
}
