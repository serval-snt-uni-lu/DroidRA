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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.cse.siis.coal.lang.ParseException;

/**
 * Compiler for COAL model. This class simply serializes COAL models. It should be called with the
 * input directory as the first argument and the destination file as the second argument.
 */
public class ModelCompiler {
  private static Logger logger = LoggerFactory.getLogger(ModelCompiler.class);

  public static void main(String[] args) throws ParseException, IOException {
    compileModel(args[0], args[1]);
  }

  /**
   * Compiles a COAL model.
   * 
   * @param inputDir The input directory that contains the COAL specification files.
   * @param outputPath The path to the output file.
   * @throws ParseException if something goes wrong with the COAL parsing.
   * @throws IOException if something goes wrong with the file operations.
   */
  private static void compileModel(String inputDir, String outputPath) throws ParseException,
      IOException {
    logger.info("Compiling model from directory " + inputDir + " to " + outputPath);
    Model.loadModelFromDirectory(inputDir);

    File outputFile = new File(outputPath);
    outputFile.getParentFile().mkdirs();
    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
    objectOutputStream.writeObject(Model.v());
    objectOutputStream.close();
    fileOutputStream.close();
  }
}
