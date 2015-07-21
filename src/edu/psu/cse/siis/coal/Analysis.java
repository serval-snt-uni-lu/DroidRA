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
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import soot.PackManager;
import edu.psu.cse.siis.coal.lang.ParseException;

/**
 * The high-level pattern for the analysis, including the analysis setup, running the analysis and
 * processing the results. Classes implementing this should override:
 * <ul>
 * <li>{@link #registerFieldTransformerFactories} to register the field transformer factories that
 * generate field transformers to represent the influence of field operations.</li>
 * <li>{@link #registerArgumentValueAnalyses} to register the argument value analyses that are used
 * to determine the values of method arguments.</li>
 * <li>{@link #registerMethodReturnValueAnalyses} to register analyses for method return values.</li>
 * <li>{@link #initializeAnalysis} to initialize the analysis.
 * <li>{@link #processResults} to process the analysis results.
 * <li>{@link #finalizeAnalysis} to finalize the analysis (e.g., closing files).
 * </ul>
 * 
 * @param <A> A generic {@link CommandLineArguments} type for command line arguments.
 */
public abstract class Analysis<A extends CommandLineArguments> {
  /**
   * Pattern method to perform the analysis.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   */
  public void performAnalysis(A commandLineArguments) {
    try {
      PropagationTimers.v().modelParsing.start();
      loadModel(commandLineArguments);
      PropagationTimers.v().modelParsing.end();
      PropagationTimers.v().misc.start();
      registerFieldTransformerFactories(commandLineArguments);
      registerArgumentValueAnalyses(commandLineArguments);
      registerMethodReturnValueAnalyses(commandLineArguments);
      setApplicationClasses(commandLineArguments);
      PropagationTimers.v().misc.end();
      initializeAnalysis(commandLineArguments);
      PropagationTimers.v().soot.start();
      PackManager.v().runPacks();
      PropagationTimers.v().soot.end();
      processResults(commandLineArguments);
      finalizeAnalysis(commandLineArguments);
    } catch (FatalAnalysisException e) {
      handleFatalAnalysisException(commandLineArguments, e);
    }
  }

  /**
   * Loads the COAL model to be used for the analysis. The model can be stored either in a directory
   * with a flat structure (no subdirectories) or in a compiled (serialized) format.
   * 
   * @param commanLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   * 
   * @see ModelCompiler
   */
  protected void loadModel(A commanLineArguments) throws FatalAnalysisException {
    try {
      if (commanLineArguments.getCompiledModel() != null) {
        Model.loadModelFromCompiledFile(commanLineArguments.getCompiledModel());
      } else if (commanLineArguments.getModel() != null) {
        Model.loadModel(commanLineArguments.getModel());
      } else {
        throw new FatalAnalysisException("No model file or directory was specified");
      }
    } catch (ClassNotFoundException | IOException | ParseException e) {
      throw new FatalAnalysisException("Could not load model", e);
    }
  }

  /**
   * Registers the field transformer factories for the analysis. A
   * {@link edu.psu.cse.siis.coal.field.transformers.FieldTransformer FieldTransformer} specifies
   * how a field is modified by a method. For each possible FieldTransformer type, a
   * {@link edu.psu.cse.siis.coal.field.transformers.FieldTransformerFactory
   * FieldTransformerFactory} should be declared to instantiate the FieldTransformer as appropriate.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected abstract void registerFieldTransformerFactories(A commandLineArguments)
      throws FatalAnalysisException;

  /**
   * Registers the method argument value analyses.
   * {@link edu.psu.cse.siis.coal.field.transformers.FieldTransformer FieldTransformer} objects are
   * generated by {@link edu.psu.cse.siis.coal.field.transformers.FieldTransformerFactory
   * FieldTransformerFactory} objects using both a COAL specification and most often a method
   * argument value. This method specifies which
   * {@link edu.psu.cse.siis.coal.arguments.ArgumentValueAnalysis ArgumentValueAnalysis} is in
   * charge of handling every argument type using the
   * {@link edu.psu.cse.siis.coal.arguments.ArgumentValueManager#registerArgumentValueAnalysis
   * registerArgumentValueAnalysis} method.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected abstract void registerArgumentValueAnalyses(A commandLineArguments)
      throws FatalAnalysisException;

  /**
   * Registers method return value analyses. When the return value of a method is always known and
   * the method should not be traversed, a
   * {@link edu.psu.cse.siis.coal.arguments.MethodReturnValueAnalysis MethodReturnValueAnalysis} can
   * be registered to specify the return value with the
   * {@link edu.psu.cse.siis.coal.arguments.MethodReturnValueManager#registerMethodReturnValueAnalysis
   * registerMethodReturnValueAnalysis} method.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected abstract void registerMethodReturnValueAnalyses(A commandLineArguments)
      throws FatalAnalysisException;

  /**
   * Registers the application classes through which the propagation should take place. By default,
   * this is the list of classes in the input directory or jar file. By default, it also includes
   * the list of modeled classes if it was requested on the command line.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected void setApplicationClasses(A commandLineArguments) throws FatalAnalysisException {
    AnalysisParameters.v().addAnalysisClasses(
        computeAnalysisClasses(commandLineArguments.getInput()));
    if (commandLineArguments.traverseModeled()) {
      AnalysisParameters.v().addAnalysisClasses(Model.v().getModeledTypes());
    }
  }

  /**
   * Initializes the analysis, including Soot parameters (pointer analysis, etc.).
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected abstract void initializeAnalysis(A commandLineArguments) throws FatalAnalysisException;

  /**
   * Processes the results of the analysis.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected abstract void processResults(A commandLineArguments) throws FatalAnalysisException;

  /**
   * Finalizes the analysis. This should be used for example to close open files.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @throws FatalAnalysisException if a fatal error occurs.
   */
  protected abstract void finalizeAnalysis(A commandLineArguments) throws FatalAnalysisException;

  /**
   * Handles a fatal analysis exception. A {@link FatalAnalysisException} occurs when something
   * serious prevents the analysis from proceeding. This exception is caught at a higher level to
   * allow some things to be wrapped up (e.g., closing files or database connections). This method
   * specifies the wrapping up process.
   * 
   * @param commandLineArguments A {@link CommandLineArguments} subclass object that represents the
   *          command line arguments for this analysis.
   * @param exception A {@link FatalAnalysisException}.
   */
  protected abstract void handleFatalAnalysisException(A commandLineArguments,
      FatalAnalysisException exception);

  /**
   * Computes the set of classes in a directory or a jar file. This method computes the fully
   * qualified Java name of all the classes under a given directory. The class files can be in
   * multiple packages.
   * 
   * @param dirOrJar A directory or jar path.
   * @return The set of classes in the input directory.
   * @throws FatalAnalysisException if something goes wrong with the file operations.
   */
  protected Set<String> computeAnalysisClasses(String dirOrJar) throws FatalAnalysisException {
    try {
      File file = new File(dirOrJar);

      if (file.isDirectory()) {
        String directoryString = file.getCanonicalPath();
        int basePos = directoryString.length() + 1;
        return computeAnalysisClassesInDir(file, basePos);
      } else {
        return computeAnalysisClassesInJar(file);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new FatalAnalysisException(e);
    }
  }

  /**
   * Helper for computing the set of classes in a directory.
   * 
   * @param directory A {@link java.io.File File} object for the directory under which classes
   *          should be searched for.
   * @param basePos The position of the first letter for the Java package names in the directory's
   *          absolute path. For example, if the current directory is <code>/home/foo/bar</code>,
   *          but the top-level directory where all Java packages are located is
   *          <code>/home/foo</code>, then the <code>basePos</code> would be 10, which is the index
   *          of the <code>b</code> character.
   * @return The set of classes found under the directory.
   * @throws IOException if something goes wrong with the file operations.
   */
  private Set<String> computeAnalysisClassesInDir(File directory, int basePos) throws IOException {
    File[] nestedFilesAndDirs = directory.listFiles();
    Set<String> result = new HashSet<String>();

    for (File nestedFile : nestedFilesAndDirs) {
      if (nestedFile.isDirectory()) {
        result.addAll(computeAnalysisClassesInDir(nestedFile, basePos));
      } else {
        String canonicalPath = nestedFile.getCanonicalPath();
        if (canonicalPath.endsWith(".class")) {
          result
              .add(canonicalPath.substring(basePos, canonicalPath.length() - 6).replace('/', '.'));
        }
      }
    }

    return result;
  }

  /**
   * Helper for computing the set of classes in a jar file.
   * 
   * @param file A {@link java.io.File File} object for the jar in which classes should be searched
   *          for.
   * @return The set of classes found in the jar.
   * @throws IOException if something goes wrong with the file operations.
   */
  private Set<String> computeAnalysisClassesInJar(File file) throws IOException {
    Set<String> result = new HashSet<>();

    JarFile jarFile = new JarFile(file);
    Enumeration<JarEntry> jarEntries = jarFile.entries();

    while (jarEntries.hasMoreElements()) {
      JarEntry jarEntry = jarEntries.nextElement();
      String entryName = jarEntry.getName();
      if (entryName.endsWith(".class")) {
        String name = entryName.substring(0, entryName.length() - 6).replace('/', '.');
        result.add(name);
      }
    }

    jarFile.close();

    return result;
  }
}
