package com.mapr.fs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

public class Util {
    private static final Logger log = Logger.getLogger(Util.class);

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return mapper;
    }

    public static void setConfigPath(String[] args) throws ParseException {
        Option option = new Option("p", "path", true, "Path to config file");
        option.setArgs(1);
        option.setOptionalArg(false);
        option.setArgName("Path to config file ");

        Options options = new Options();
        options.addOption(option);

        CommandLineParser cmdLinePosixParser = new PosixParser();
        CommandLine commandLine = cmdLinePosixParser.parse(options, args);

        if (commandLine.hasOption("p")) {
            String[] arguments = commandLine.getOptionValues("p");
            Config.addConfigPath(arguments);
            log.info("Find config file in " + arguments[0]);
        }
    }
}
