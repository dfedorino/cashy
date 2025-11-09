package com.dfedorino.cashy.ui.cli.command.impl;

import com.dfedorino.cashy.scenario.ScenarioService;
import com.dfedorino.cashy.scenario.dto.ScenarioResult;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class EditCategory implements Command<CategoryDto> {

    public static final String KEY_TOKEN = "category_edit";
    public static final String NAME_TOKEN = "-n";
    public static final String LIMIT_TOKEN = "-l";
    public static final String EXAMPLE_MESSAGE1 =
            KEY_TOKEN + " food " + NAME_TOKEN + " groceries";
    public static final String EXAMPLE_MESSAGE2 =
            KEY_TOKEN + " food " + LIMIT_TOKEN + " 5000";
    public static final String SUCCESS_MESSAGE = "Category edited successfully!";
    public static final String DESCRIPTION_MESSAGE = "Edit an existing category name or budget";

    private final ScenarioService scenarioService;

    @Override
    public ResultWithNotification<CategoryDto> apply(String... commandAndArgs) {
        String category = commandAndArgs[1];
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 4 &&
                                                Objects.equals(args[0], KEY_TOKEN) &&
                                                (Objects.equals(args[2], NAME_TOKEN) ||
                                                        Objects.equals(args[2], LIMIT_TOKEN)),
                                        commandAndArgs)
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        ScenarioResult<CategoryDto> scenarioResult;
        String limit = commandAndArgs[3];
        if (Objects.equals(commandAndArgs[2], LIMIT_TOKEN)) {
            var limitFailedCheck = validateAmount(limit);
            if (limitFailedCheck.isPresent()) {
                return limitFailedCheck.get();
            }

            scenarioResult = scenarioService.editBudget(category, new BigDecimal(limit));
        } else {
            String newCategoryName = commandAndArgs[3];
            scenarioResult = scenarioService.editBudget(category, newCategoryName);
        }

        return scenarioResult.isSuccess() ?
                ResultWithNotification.ofPayload(SUCCESS_MESSAGE, scenarioResult.result()) :
                ResultWithNotification.ofErrorMessage(scenarioResult.failureReason().getMessage());
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
        return EXAMPLE_MESSAGE1 + System.lineSeparator() + EXAMPLE_MESSAGE2;
    }
}
