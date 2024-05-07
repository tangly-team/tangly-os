/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.ui;

import com.github.mvysny.vaadinboot.VaadinBoot;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point to the start of the regular Java SE application with an embedded Jetty server. The application parameters are:
 * <dl>
 *     <dt>port</dt>
 *     <dd>The parameter defines the listening port of the Jetty embedded server.</dd>
 *     <dt>mode</dt>
 *     <dd>The parameter defines the mode of the application.
 *     The application is either using an in-memory data or has a persistent storage where import data can be provided and updated stored in the database.</dd>
 * </dl>
 */
public class Main {
    private static final Logger logger = LogManager.getLogger();
    private static int port = 8080;
    private static boolean isInMemory = true;

    public static void main(@NotNull String[] args) throws Exception {
        final String contextRoot = "/erp";
        parse(args);
        new VaadinBoot().setPort(port).withContextRoot(contextRoot).run();
    }

    private static Options options() {
        var options = new Options();
        options.addOption(Option.builder("h").longOpt("help").hasArg(false).desc("print this help message").build());
        options.addOption(Option.builder("p").longOpt("port").type(Integer.TYPE).argName("port").hasArg().desc("listening port of the embedded server").build());
        options.addOption(
            Option.builder("c").longOpt("configuration").type(String.class).argName("configuration-file").hasArg().desc("path to the applicaiton configuration file").build());
        options.addOption(Option.builder("m").longOpt("mode").argName("mode").hasArg().desc("mode of the application").build());
        return options;
    }

    private static void parse(@NotNull String[] args) {
        var parser = new DefaultParser();
        var options = options();
        try {
            var line = parser.parse(options, args);
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("tangly ERP", options);
            }
            port = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : 8080;
            isInMemory = line.hasOption("m") || Boolean.parseBoolean(line.getOptionValue("m"));
        } catch (NumberFormatException | ParseException e) {
            logger.atError().log("Parsing failed.  Reason: {}", e.getMessage());
        }
    }
}
