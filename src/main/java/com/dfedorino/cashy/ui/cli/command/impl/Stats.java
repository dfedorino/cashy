package com.dfedorino.cashy.ui.cli.command.impl;

import com.dfedorino.cashy.scenario.ScenarioService;
import com.dfedorino.cashy.scenario.dto.ScenarioResult;
import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import com.dfedorino.cashy.ui.cli.command.util.FileUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class Stats implements Command<StatsDto> {

    public static final String KEY_TOKEN = "stats";
    public static final String CATEGORIES_TOKEN = "-c";
    public static final String EXPORT_TOKEN = "-e";
    public static final String SUCCESS_MESSAGE = "Statistics fetched successfully!";
    public static final String DESCRIPTION_MESSAGE = "Fetch budget statistics";
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

        List<String> commandsList = Arrays.asList(commandAndArgs);

        if (commandsList.contains(CATEGORIES_TOKEN)) {
            return filteredStats(commandAndArgs, commandsList);
        }

        return fullStats(commandsList);
    }

    private ResultWithNotification<StatsDto> fullStats(
            List<String> commandsList) {
        ScenarioResult<StatsDto> scenarioResult;
        scenarioResult = scenarioService.stats();

        if (!scenarioResult.isSuccess()) {
            return ResultWithNotification.ofErrorMessage(
                    scenarioResult.failureReason().getMessage());
        }

        tryExportToJson(commandsList, scenarioResult);
        return ResultWithNotification.ofPayload(SUCCESS_MESSAGE,
                                                scenarioResult.result());
    }

    private static void tryExportToJson(List<String> commandsList,
                                  ScenarioResult<StatsDto> scenarioResult) {
        if (commandsList.contains(EXPORT_TOKEN)) {
            FileUtils.exportToJson(scenarioResult.result(), "stats");
        }
    }

    private ResultWithNotification<StatsDto> filteredStats(
            String[] commandAndArgs, List<String> commandsList) {
        java.util.Optional<ResultWithNotification<StatsDto>> failedArgsCheck;
        ScenarioResult<StatsDto> scenarioResult;
        failedArgsCheck = validateArguments(
                args -> Objects.equals(args[1], CATEGORIES_TOKEN),
                commandAndArgs);

        if (failedArgsCheck.isPresent()) {
            return failedArgsCheck.get();
        }
        Set<String> distinctCategories = new HashSet<>(
                commandsList.subList(2, commandAndArgs.length));

        distinctCategories.remove(EXPORT_TOKEN);

        if (distinctCategories.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.COMMAND_INVALID_MESSAGE.formatted(example()));
        }

        scenarioResult = scenarioService.stats(distinctCategories);

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
        tryExportToJson(commandsList, scenarioResult);
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
