/*
 * Copyright 2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LedgerChannelText extends CmdChannel {
    final static String BOOKING_DESCRIPTION = "[account=account-id,amount=xx.xx,text=\"text of the booking\"]";
    final static String CMD_BOOK_TRANSACTION = "book-transaction";
    final static String CMD_BOOK_SPLIT_TRANSACTION = "book-split-transaction";
    final static String QUERY_GET_ACCOUNT_BALANCE = "get-account-balance";

    final LedgerInterpreter interpreter;
    final Map<String, Options> commands;

    public LedgerChannelText(@NotNull LedgerInterpreter interpreter) {
        this.interpreter = interpreter;
        this.commands = createCommands();
    }

    @Override
    public boolean canProcess(String command) {
        return CMD_BOOK_TRANSACTION.equals(command) || CMD_BOOK_SPLIT_TRANSACTION.equals(command) || QUERY_GET_ACCOUNT_BALANCE.equals(command);
    }

    public void process(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            if ("help".equals(args[0])) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth(120);
                commands.keySet().forEach(o -> formatter.printHelp(o, commands.get(o), true));
            } else {
                CommandLine line = parser.parse(commands.get(args[0]), args);
                LedgerCmd cmd = switch (args[0]) {
                    case CMD_BOOK_TRANSACTION ->
                        new CmdBookTransaction(generatedId(), line.getOptionValue("from"), line.getOptionValue("to"), LocalDate.parse(line.getOptionValue("date")),
                            new BigDecimal(line.getOptionValue("amount")), line.getOptionValue("text"));
                    case CMD_BOOK_SPLIT_TRANSACTION -> null;
                    case QUERY_GET_ACCOUNT_BALANCE -> new CmdGetAccountBalance(generatedId(), line.getOptionValue("account"), LocalDate.parse(line.getOptionValue("date")));
                    default -> null;
                };
                if (cmd != null) {
                    interpreter.execute(cmd, this);
                }
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    @Override
    public void transmit(int cmdId, Object payload) {
        switch (payload) {
            case AccountBalance balance -> System.out.println(balance);
            default -> throw new IllegalArgumentException("Unexpected type: " + payload);
        }
    }

    private static Booking toBooking(@NotNull String input) {
        var tokens = input.split("\\[|=|,|\\]");
        if (tokens[1].equals("account") && tokens[3].equals("amount") && tokens[5].equals("text")) {
            String accountId = tokens[2];
            String amount = tokens[4];
            String text = tokens[6];
            return new Booking(accountId, new BigDecimal(amount), text.isEmpty() ? null : text);
        } else {
            throw new IllegalArgumentException(input);
        }
    }

    private static Map<String, Options> createCommands() {
        Map<String, Options> commands = new HashMap<>();

        Options options = new Options();
        options.addOption(Option.builder("from").hasArg().argName("Account ID from").required().build());
        options.addOption(Option.builder("to").hasArg().argName("Account ID to").required().build());
        options.addOption(Option.builder("date").hasArg().argName("date").required().build());
        options.addOption(Option.builder("amount").hasArg().argName("amount").required().build());
        options.addOption(Option.builder("text").hasArg().argName("text").build());
        commands.put(CmdBookTransaction.NAME, options);

        options = new Options();
        options.addOption(Option.builder("from").hasArgs().argName("bookings-from").desc(BOOKING_DESCRIPTION).required().build());
        options.addOption(Option.builder("to").hasArgs().argName("bookings-to").desc(BOOKING_DESCRIPTION).required().build());
        options.addOption(Option.builder("date").hasArg().argName("date").required().build());
        options.addOption(Option.builder("amount").hasArg().argName("amount").required().build());
        options.addOption(Option.builder("text").hasArg().argName("text").build());
        commands.put(CmdBookSplitTransaction.NAME, options);

        options = new Options();
        options.addOption(Option.builder("account").hasArg().argName("Account ID").required().build());
        options.addOption(Option.builder("date").hasArg().argName("date").required().build());
        commands.put(CmdGetAccountBalance.NAME, options);
        return commands;
    }
}