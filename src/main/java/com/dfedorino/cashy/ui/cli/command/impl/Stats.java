package com.dfedorino.cashy.ui.cli.command.impl;

import com.dfedorino.cashy.scenario.ScenarioService;
import com.dfedorino.cashy.scenario.dto.ScenarioResult;
import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class Stats implements Command<StatsDto> {

    public static final String KEY_TOKEN = "stats";
    public static final String CATEGORIES_TOKEN = "-c";
    public static final String SUCCESS_MESSAGE = "Statistics fetched successfully!";
    public static final String DESCRIPTION_MESSAGE = "Budget statistics";
    public static final String FAILED_TO_FIND_CATEGORIES = "Failed to find categories: ";
    public static final String NOT_ALL_CATEGORIES_FOUND = "Not all categories found!";

    private final ScenarioService scenarioService;

    @Override
    public ResultWithNotification<StatsDto> apply(String... commandAndArgs) {
        var failedArgsCheck = validateArguments(
                args -> Objects.equals(args[0], KEY_TOKEN),
                commandAndArgs);

        if (failedArgsCheck.isPresent()) {
            return failedArgsCheck.get();
        }

        if (commandAndArgs.length == 1) {
            ScenarioResult<StatsDto> scenarioResult = scenarioService.stats();

            return scenarioResult.isSuccess() ?
                    ResultWithNotification.ofPayload(SUCCESS_MESSAGE,
                                                     scenarioResult.result()) :
                    ResultWithNotification.ofErrorMessage(
                            scenarioResult.failureReason().getMessage());
        }

        failedArgsCheck = validateArguments(
                args -> Objects.equals(args[1], CATEGORIES_TOKEN),
                commandAndArgs);

        if (failedArgsCheck.isPresent()) {
            return failedArgsCheck.get();
        }

        Set<String> distinctCategories = new HashSet<>(
                Arrays.asList(commandAndArgs).subList(2, commandAndArgs.length));

        if (distinctCategories.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.COMMAND_INVALID_MESSAGE.formatted(example()));
        }

        ScenarioResult<StatsDto> scenarioResult = scenarioService.stats(distinctCategories);

        if (!scenarioResult.isSuccess()) {
            return ResultWithNotification.ofErrorMessage(
                    scenarioResult.failureReason().getMessage());
        }

        if (scenarioResult.result().expenseCategories().isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    FAILED_TO_FIND_CATEGORIES + distinctCategories);
        }

        if (scenarioResult.result().expenseCategories().size() != distinctCategories.size()) {
            return ResultWithNotification.ofPayload(NOT_ALL_CATEGORIES_FOUND,
                                                    scenarioResult.result());
        }

        return ResultWithNotification.ofPayload(SUCCESS_MESSAGE,
                                                scenarioResult.result());

    }

    @Override
    public String key() {
        return KEY_TOKEN;
    }

    @Override
    public String description() {
        return DESCRIPTION_MESSAGE;
    }

    @Override
    public String example() {
        return KEY_TOKEN + " " + CATEGORIES_TOKEN + " groceries taxi";
    }
}
