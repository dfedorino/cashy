package com.dfedorino.cashy.ui.cli.command.util;

import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.ui.cli.CommandLineInterface;
import com.dfedorino.cashy.ui.cli.command.Command;
import de.vandermeer.asciitable.AsciiTable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    public void render(StatsDto stats) {

        if (!stats.incomeCategories().isEmpty()) {
            AsciiTable incomeTable = getIncomeAsciiTable(stats);
            log.info("Доходы: {}{}", System.lineSeparator(), incomeTable.render());
        }

        if (!stats.expenseCategories().isEmpty()) {
            AsciiTable expenseTable = getExpenseAsciiTable(stats);
            log.info("Расходы: {}{}", System.lineSeparator(), expenseTable.render());
        }
    }

    private static AsciiTable getExpenseAsciiTable(StatsDto stats) {
        AsciiTable expenseTable = new AsciiTable();
        expenseTable.addRule();
        expenseTable.addRow("Категория", "Израсходовано", "Бюджет", "Остаток бюджета");
        expenseTable.addRule();

        for (var category : stats.expenseCategories()) {
            expenseTable.addRow(category.categoryName(),
                                category.currentBalance().toPlainString(),
                                getValueOrDash(category.limit()),
                                getValueOrDash(category.remainingBalance()));
        }
        expenseTable.addRule();
        expenseTable.addRow("Итого", stats.totalExpenseAmount(), "-", "-");
        expenseTable.addRule();
        return expenseTable;
    }

    private static AsciiTable getIncomeAsciiTable(StatsDto stats) {
        AsciiTable incomeTable = new AsciiTable();
        incomeTable.addRule();
        incomeTable.addRow("Категория", "Добавлено");
        incomeTable.addRule();

        for (var category : stats.incomeCategories()) {
            incomeTable.addRow(category.categoryName(),
                               category.currentBalance().toPlainString());
        }
        incomeTable.addRule();
        incomeTable.addRow("Итого", stats.totalIncomeAmount());
        incomeTable.addRule();
        return incomeTable;
    }

    private static String getValueOrDash(BigDecimal value) {
        return Optional.ofNullable(value)
                .map(BigDecimal::toPlainString)
                .orElse("-");
    }
}
