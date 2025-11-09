package com.dfedorino.cashy.ui.cli.command.util;

import com.dfedorino.cashy.ui.cli.CommandLineInterface;
import com.dfedorino.cashy.ui.cli.command.Command;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ConsoleUtils {

    public void printOutAvailableCommands(List<Command<?>> commands) {
        log.info(CommandLineInterface.AVAILABLE_COMMANDS);

        commands.forEach(command -> log.info(
                CommandLineInterface.COMMAND_LINE_TEMPLATE,
                command.key(),
                command.description(),
                command.example()
        ));
    }
}
