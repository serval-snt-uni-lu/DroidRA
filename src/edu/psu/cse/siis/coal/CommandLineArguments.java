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
import org.apache.commons.cli.ParseException;

/**
 * The command line arguments for the analysis.
 */
public class CommandLineArguments {
  private CommandLine commandLine;
  private String model;
  private String compiledModel;
  private String classpath;
  private String input;
  private String output;
  private boolean traverseModeled = false;

  /**
   * Gets the path to the directory containing the model.
   *
   * @return The path to the model.
   */
  public String getModel() {
    return model;
  }

  /**
   * Sets the <code>CommandLine</code> object this depends on.
   *
   * @param commandLine A <code>CommandLine</code> object.
   */
  public void setCommandLine(CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  /**
   * Sets the path to the directory containing the model. This should be a single directory
   * containing all COAL specifications.
   * 
   * @param model The path to the model.
   * 
   * @see #getModel()
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * Gets the path to the compiled model.
   * 
   * @return The path to the compiled model.
   * 
   * @see #setCompiledModel
   */
  public String getCompiledModel() {
    return compiledModel;
  }

  /**
   * Sets the path to the compiled model.
   * 
   * @param compiledModel The path to the compiled model.
   * 
   * @see #getCompiledModel
   */
  public void setCompiledModel(String compiledModel) {
    this.compiledModel = compiledModel;
  }

  /**
   * Gets the classpath for the analysis.
   * 
   * @return The classpath for the analysis.
   * 
   * @see #setClasspath
   */
  public String getClasspath() {
    return classpath;
  }

  /**
   * Sets the classpath for the analysis. The directories and jar files on the classpath should
   * contain all classes that are referenced by the input classes but through which propagation
   * should not be performed. For example, this includes library or framework classes. Multiple
   * paths should be separated with <code>:</code>.
   * 
   * @param classpath The classpath for the analysis.
   * 
   * @see #getClasspath
   */
  public void setClasspath(String classpath) {
    this.classpath = classpath;
  }

  /**
   * Gets the input directory or file for the analysis.
   * 
   * @return The input directory or file for the analysis.
   * 
   * @see #setInput
   */
  public String getInput() {
    return input;
  }

  /**
   * Sets the input directories or files for the analysis. They should contain all classes through
   * which the propagation should be performed. Multiple entries should be separated with
   * <code>:</code>.
   * 
   * @param input The input directory or file
   * 
   * @see #getInput
   */
  public void setInput(String input) {
    this.input = input;
  }

  /**
   * Gets the output directory or file for the analysis.
   * 
   * @return The output directory or file
   * 
   * @see #setOutput
   */
  public String getOutput() {
    return output;
  }

  /**
   * Sets the output directory or file for the analysis.
   * 
   * @param output The output directory or file.
   * 
   * @see #getOutput
   */
  public void setOutput(String output) {
    this.output = output;
  }

  /**
   * Sets the flag that determines if propagation should be done through the modeled classes.
   * 
   * @param traverseModeled The value of the flag.
   */
  public void setTraverseModeled(boolean traverseModeled) {
    this.traverseModeled = traverseModeled;
  }

  /**
   * Determines whether the propagation should be done through the modeled classes. This is
   * equivalent to adding the modeled classes to the list of analysis classes.
   * 
   * @return True if the propagation should be performed through the analysis classes.
   */
  public boolean traverseModeled() {
    return traverseModeled;
  }

  /**
   * Determines if an option has been set.
   * 
   * @param opt Character name of the option.
   * @return True if the option has been set.
   */
  public boolean hasOption(char opt) {
    return commandLine.hasOption(opt);
  }

  /**
   * Determines if an option has been set.
   * 
   * @param opt Name of the option.
   * @return True if the option has been set.
   */
  public boolean hasOption(String opt) {
    return commandLine.hasOption(opt);
  }

  /**
   * Retrieves the argument, if any, of an option.
   * 
   * @param opt Character name of the option.
   * @return The value of the argument if the option is set and has an argument, otherwise null.
   */
  public String getOptionValue(char opt) {
    return commandLine.getOptionValue(opt);
  }

  /**
   * Retrieves the argument, if any, of an option.
   * 
   * @param opt Name of the option.
   * @return The value of the argument if the option is set and has an argument, otherwise null.
   */
  public String getOptionValue(String opt) {
    return commandLine.getOptionValue(opt);
  }

  /**
   * Retrieves the argument, if any, of an option.
   * 
   * @param opt Character name of the option.
   * @param defaultValue The default value to be returned if the option is not specified.
   * @return The value of the argument if the option is set and has an argument, otherwise
   *         <code>defaultValue</code>.
   */
  public String getOptionValue(char opt, String defaultValue) {
    return commandLine.getOptionValue(opt, defaultValue);
  }

  /**
   * Retrieves the argument, if any, of an option.
   * 
   * @param opt Name of the option.
   * @param defaultValue The default value to be returned if the option is not specified.
   * @return The value of the argument if the option is set and has an argument, otherwise
   *         <code>defaultValue</code>.
   */
  public String getOptionValue(String opt, String defaultValue) {
    return commandLine.getOptionValue(opt, defaultValue);
  }

  /**
   * Retrieves the array of values, if any, of an option.
   * 
   * @param opt Name of the option.
   * @return The values of the argument if the option is set and has an argument, otherwise null.
   */
  public String[] getOptionValues(char opt) {
    return commandLine.getOptionValues(opt);
  }

  /**
   * Retrieves the array of values, if any, of an option.
   * 
   * @param opt Character name of the option.
   * @return The values of the argument if the option is set and has an argument, otherwise null.
   */
  public String[] getOptionValues(String opt) {
    return commandLine.getOptionValues(opt);
  }

  /**
   * Retrieves a version of an option converted to a particular type.
   * 
   * @param opt Name of the option.
   * @return The value of the argument if the option is set and has an argument, otherwise null.
   * @throws ParseException if the argument value cannot be converted to the specified type.
   */
  public Object getParsedOptionValue(String opt) throws ParseException {
    return commandLine.getParsedOptionValue(opt);
  }
}
