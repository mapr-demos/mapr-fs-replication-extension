package com.mapr.fs.utils;

import com.mapr.fs.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.File;

@Slf4j
public class ConfigUtil {

    public static void setConfigPath(String[] args) throws ParseException {
        try {
            Option option = new Option("p", "path", true, "Path to config file");
            option.setArgs(1);
            option.setOptionalArg(false);
            option.setRequired(true);
            option.setArgName("Path to config file ");

            Options options = new Options();
            options.addOption(option);

            CommandLineParser cmdLinePosixParser = new PosixParser();
            CommandLine commandLine = cmdLinePosixParser.parse(options, args);

            if (commandLine.hasOption("p")) {
                String[] arguments = commandLine.getOptionValues("p");
                checkAndSetConfigPath(arguments[0]);
            }
        } catch (MissingOptionException | MissingArgumentException ex) {
            log.error("Please, add path to config.conf file");
            System.exit(0);
        }
    }

    private static void checkAndSetConfigPath(String argument) {
        File file = new File(argument);
        if (file.exists() && file.isFile()) {
            Config.addConfigPath(argument);
        } else {
            log.info("Cannot find config file in " + argument);
            throw new RuntimeException("Cannot find config file");
        }
    }
}
