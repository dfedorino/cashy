package com.dfedorino.cashy.ui.cli.command.impl;

import com.dfedorino.cashy.scenario.ScenarioService;
import com.dfedorino.cashy.scenario.dto.ScenarioResult;
import com.dfedorino.cashy.service.dto.OperationDto;
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
public final class Income implements Command<OperationDto> {

    public static final String KEY_TOKEN = "income";
    public static final String EXAMPLE_MESSAGE =
            KEY_TOKEN + " salary 100000";
    public static final String SUCCESS_MESSAGE = "Income operation performed successfully!";
    public static final String DESCRIPTION_MESSAGE = "Perform an income operation";

    private final ScenarioService scenarioService;

    @Override
    public ResultWithNotification<OperationDto> apply(String... commandAndArgs) {
        String incomeCategory = commandAndArgs[1];
        String incomeAmount = commandAndArgs[2];
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 3 &&
                                                Objects.equals(args[0], KEY_TOKEN),
                                        commandAndArgs),
                () -> validateAmount(incomeAmount)
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        ScenarioResult<OperationDto> scenarioResult =
                scenarioService.topUp(incomeCategory,
                                      new BigDecimal(incomeAmount));

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
