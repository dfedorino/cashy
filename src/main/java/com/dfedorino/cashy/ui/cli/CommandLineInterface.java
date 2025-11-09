package com.dfedorino.cashy.ui.cli;

import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import com.dfedorino.cashy.ui.cli.command.impl.Quit;
import com.dfedorino.cashy.ui.cli.command.util.ConsoleUtils;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandLineInterface {
    public static final String TITLE = ">> Cashy Project <<";
    public static final String AVAILABLE_COMMANDS = "Available commands:";
    public static final String COMMAND_LINE_TEMPLATE = "* {} : {}, example: '{}'";
    public static final String INVALID_COMMAND = "!!! Invalid command !!!";
    private final List<Command<?>> commands;

    public void start() {
        var keyToCommand = commands.stream()
                .collect(Collectors.toMap(Command::key, Function.identity()));

        log.info(TITLE);
        ConsoleUtils.printOutAvailableCommands(commands);

        var scanner = new Scanner(System.in);

        for (String line = scanner.nextLine(); !line.equalsIgnoreCase(Quit.KEY_TOKEN);
                line = scanner.nextLine()) {
            String[] tokens = line.split(" ");
            String command = tokens[0];
            if (!keyToCommand.containsKey(command)) {
                log.info(INVALID_COMMAND);
                ConsoleUtils.printOutAvailableCommands(commands);
            } else {
                ResultWithNotification<?> resultWithNotification = keyToCommand.get(command).apply(tokens);
                log.info(resultWithNotification.notification());
                resultWithNotification.result().ifPresent(value -> {
                    // TODO: handle results
                });
            }
        }
    }

}
