/*
 * Copyright (C) 2015 The Pennsylvania State University and the University of Wisconsin
 * Systems and Internet Infrastructure Security Laboratory
 *
 * Author: Damien Octeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.psu.cse.siis.coal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract parser for command line options. Subclasses should implement
 * {@link #parseAnalysisSpecificArguments} and {@link #printHelp}.
 * 
 * @param <A> A {@link CommandLineArguments} parameter.
 */
public abstract class CommandLineParser<A extends CommandLineArguments> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Parses the command line options. This method parses default options that are common to all
   * analyses and it also causes analysis-specific options to be processed.
   * 
   * @param args The command line arguments.
   * @param clazz The class type of the {@link CommandLineArguments} that should be returned.
   * @return The parsed command line arguments.
   */
  public A parseCommandLine(String[] args, Class<A> clazz) {
    Options options = new Options();

    parseDefaultCommandLineArguments(options);
    parseAnalysisSpecificArguments(options);

    CommandLine commandLine = null;
    try {
      org.apache.commons.cli.CommandLineParser commandLineParser = new GnuParser();
      commandLine = commandLineParser.parse(options, args);
    } catch (ParseException e) {
      printHelp(options);
      logger.error("Could not parse command line arguments", e);
      return null;
    }

    A commandLineArguments = null;
    try {
      commandLineArguments = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      logger.error("Could not instantiate type " + clazz, e);
      return null;
    }
    commandLineArguments.setCommandLine(commandLine);
    commandLineArguments.setModel(commandLine.getOptionValue("model"));
    commandLineArguments.setCompiledModel(commandLine.getOptionValue("cmodel"));
    commandLineArguments.setInput(commandLine.getOptionValue("in"));
    commandLineArguments.setClasspath(String.format("%s:%s:", commandLine.getOptionValue("cp"),
        commandLineArguments.getInput()));
    commandLineArguments.setOutput(commandLine.getOptionValue("out"));
    commandLineArguments.setTraverseModeled(commandLine.hasOption("traversemodeled"));
    AnalysisParameters.v().setInferNonModeledTypes(!commandLine.hasOption("modeledtypesonly"));

    return commandLineArguments;
  }

  /**
   * Populates the analysis-specific command line options.
   * 
   * @param options The options that should be populated.
   */
  protected abstract void parseAnalysisSpecificArguments(Options options);

  /**
   * Specifies how the help message should be printed.
   * 
   * @param options The options that should be used to print the help message.
   */
  protected abstract void printHelp(Options options);

  /**
   * Populates the default command line arguments that are common to all analyses.
   * 
   * @param options The command line options object that should be modified.
   */
  @SuppressWarnings("static-access")
  private void parseDefaultCommandLineArguments(Options options) {
    OptionGroup modelGroup = new OptionGroup();
    modelGroup.addOption(OptionBuilder.withDescription("Path to the model directory.").hasArg()
        .withArgName("model directory").create("model"));
    modelGroup.addOption(OptionBuilder.withDescription("Path to the compiled model.").hasArg()
        .withArgName("compiled model").create("cmodel"));
    modelGroup.setRequired(false);

    options.addOptionGroup(modelGroup);

    options.addOption(OptionBuilder.withDescription("The classpath for the analysis.").hasArg()
        .withArgName("classpath").isRequired().withLongOpt("classpath").create("cp"));
    options.addOption(OptionBuilder.withDescription("The input code for the analysis.").hasArg()
        .withArgName("input").isRequired().withLongOpt("input").create("in"));
    options.addOption(OptionBuilder.withDescription("The output directory or file.").hasArg()
        .withArgName("output").withLongOpt("output").create("out"));
    options.addOption(OptionBuilder.withDescription("Propagate through modeled classes.")
        .hasArg(false).create("traversemodeled"));
    options.addOption("modeledtypesonly", false, "Only infer modeled types.");
  }
}
