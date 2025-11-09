package com.dfedorino.cashy.ui.cli.command.impl;

import com.dfedorino.cashy.scenario.ScenarioService;
import com.dfedorino.cashy.scenario.dto.ScenarioResult;
import com.dfedorino.cashy.service.dto.UserDto;
import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Login implements Command<String> {

    public static final String KEY_TOKEN = "login";
    public static final String PASSWORD_TOKEN = "-p";
    public static final String EXAMPLE_MESSAGE =
            KEY_TOKEN + " my_login " + PASSWORD_TOKEN + " my_password";
    public static final String SUCCESS_MESSAGE = "Login successful!";
    public static final String DESCRIPTION_MESSAGE = "Login with user's login and password";

    private final ScenarioService scenarioService;

    @Override
    public ResultWithNotification<String> apply(String... commandAndArgs) {
        var argsValidation = validateArguments(args -> args.length == 4 &&
                                                       Objects.equals(args[0], KEY_TOKEN) &&
                                                       Objects.equals(args[2], PASSWORD_TOKEN),
                                               commandAndArgs);

        if (argsValidation.isPresent()) {
            return argsValidation.get();
        }

        ScenarioResult<UserDto> scenarioResult = scenarioService.login(commandAndArgs[1],
                                                                       commandAndArgs[3]);

        return scenarioResult.isSuccess() ?
                ResultWithNotification.ofPayload(SUCCESS_MESSAGE, scenarioResult.result().login()) :
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
