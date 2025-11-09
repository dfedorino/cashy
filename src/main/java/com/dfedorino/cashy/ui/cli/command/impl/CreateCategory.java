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
public final class CreateCategory implements Command<CategoryDto> {

    public static final String KEY_TOKEN = "category_create";
    public static final String ALERT_TOKEN = "alert";
    public static final String EXAMPLE_MESSAGE =
            KEY_TOKEN + " food 30000 " + ALERT_TOKEN + " 80";
    public static final String SUCCESS_MESSAGE = "Category created successfully!";
    public static final String DESCRIPTION_MESSAGE = "Create a new category with the given budget and alert threshold";

    private final ScenarioService scenarioService;

    @Override
    public ResultWithNotification<CategoryDto> apply(String... commandAndArgs) {
        String category = commandAndArgs[1];
        String limit = commandAndArgs[2];
        String alertThreshold = commandAndArgs[4];
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 5 &&
                                                Objects.equals(args[0], KEY_TOKEN),
                                        commandAndArgs),
                () -> validateAmount(limit),
                () -> validateAlertThreshold(alertThreshold)
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        ScenarioResult<CategoryDto> scenarioResult =
                scenarioService.budget(category,
                                       new BigDecimal(limit),
                                       Integer.parseInt(alertThreshold));

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
        return EXAMPLE_MESSAGE;
    }
}
