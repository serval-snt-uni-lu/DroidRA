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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.lang.ParseException;
import edu.psu.cse.siis.coal.lang.PropagationParser;

/**
 * Singleton that represents the COAL model for the analysis.
 */
public class Model implements Serializable {
  private static final long serialVersionUID = 1L;

  private static Model instance;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Set<String> modeledTypes = new HashSet<>();
  private Map<String, MethodDescription> signatureToArgumentsMap = new HashMap<>();
  private Map<String, MethodDescription> genSignatureToArgumentsMap = new HashMap<>();
  private Map<String, MethodDescription> copySignatureToArgumentsMap = new HashMap<>();
  private Map<String, MethodDescription> sourceSignatureToArgumentsMap = new HashMap<>();
  private Map<String, Argument[]> copyConstructors = new HashMap<>();
  private Map<String, Argument[]> staticFieldToArgumentsMap = new HashMap<>();
  private Map<String, MethodDescription> queryToMethodDescriptionMap = new HashMap<>();
  private Set<String> excludedClasses = new HashSet<>();

  /**
   * Returns the singleton instance of this class. This class should be first initialized using
   * {@link #loadModelFromCompiledFile}, {@link #loadModelFromDirectory} or
   * {@link #loadModelFromFile}.
   * 
   * @return The singleton instance.
   */
  public static Model v() {
    if (instance == null) {
      throw new RuntimeException("Model was not initialized. Please make sure you call "
          + "loadModelFromDirectory(), loadModelFromFile() or loadCompiledModelFromFile()");
    }
    return instance;
  }

  /**
   * Returns the set of all fully-qualified types modeled using COAL.
   * 
   * @return The set of all types modeled using COAL.
   */
  public Set<String> getModeledTypes() {
    return modeledTypes;
  }

  /**
   * Loads the model from a sequence of paths, which can either be directories or files. They should
   * be separated by the platform's path separator (':' in most platforms).
   * 
   * @param modelPaths A sequence of paths to model files or directories.
   * @throws FileNotFoundException if something goes wrong with the file operations.
   * @throws ParseException if something goes wrong with the parsing process.
   */
  public static void loadModel(String modelPaths) throws FileNotFoundException, ParseException {
    String[] modelPathsParts = modelPaths.split(File.pathSeparator);

    for (String modelPath : modelPathsParts) {
      File file = new File(modelPath);
      if (file.isDirectory()) {
        loadModelFromDirectory(modelPath);
      } else {
        loadModelFromFile(modelPath);
      }
    }
  }

  /**
   * Loads the model from a directory containing all COAL specifications for the analysis. The COAL
   * files should all be in that same directory (no subdirectories). This should only be called
   * once, as it calls {@link #endInitialization()}, which prevents any further modification to the
   * model.
   * 
   * @param modelDir The path to a directory containing COAL model files.
   * @throws FileNotFoundException if something goes wrong with the file operations.
   * @throws ParseException if something goes wrong with the parsing process.
   */
  public static void loadModelFromDirectory(String modelDir) throws FileNotFoundException,
      ParseException {
    if (instance == null) {
      instance = new Model();
    }

    PropagationParser.parseModelFromDirectory(instance, modelDir);
    instance.endInitialization();
  }

  /**
   * Loads the COAL model from a single file. This can be called multiple times. After all COAL
   * files have been loaded, method {@link #endInitialization()} should be called.
   * 
   * @param modelFilePath The path to a COAL specification file.
   * @throws FileNotFoundException if something goes wrong with the file operations.
   * @throws ParseException if something goes wrong with the parsing process.
   */
  public static void loadModelFromFile(String modelFilePath) throws FileNotFoundException,
      ParseException {
    if (instance == null) {
      instance = new Model();
    }

    PropagationParser.parseModelFromFile(instance, new File(modelFilePath));
  }

  /**
   * Loads the COAL model from a compiled COAL file. For the moment, this is simply a serialized
   * Model object. There is no need to call {@link #endInitialization()} after this. This method can
   * read a serialized file from inside a jar archive, on the condition that the path to the file
   * starts with <code>/res/</code>. Compiling the model can be done using the {@link ModelCompiler}
   * class.
   * 
   * @param compiledModelFilePath The path to a compiled model file.
   * @throws IOException if something goes wrong with the file operations.
   * @throws ClassNotFoundException if something goes wrong with the deserialization.
   */
  public static void loadModelFromCompiledFile(String compiledModelFilePath) throws IOException,
      ClassNotFoundException {
    InputStream inputStream;
    if (compiledModelFilePath.startsWith("/res/")) {
      inputStream = Model.class.getResourceAsStream(compiledModelFilePath);
    } else {
      inputStream = new FileInputStream(compiledModelFilePath);
    }

    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
    instance = (Model) objectInputStream.readObject();
  }

  /**
   * Determines if a type is modeled using COAL.
   * 
   * @param type A fully-qualified class type.
   * @return True if the type is modeled using COAL.
   */
  public boolean isModeledType(String type) {
    return modeledTypes.contains(type);
  }

  /**
   * Returns the arguments for a potential modifier method.
   * 
   * @param invokeExpr An invoke expression.
   * @return An array of arguments if the invoke expression is for a modifier, null otherwise.
   */
  public Argument[] getArgumentsForMethod(InvokeExpr invokeExpr) {
    return getArgumentsFromMethodDescription(signatureToArgumentsMap, invokeExpr);
  }

  /**
   * Returns the arguments for a potential generating modifier method.
   * 
   * @param invokeExpr An invoke expression.
   * @return An array of arguments if the invoke expression is for a generating modifier, null
   *         otherwise.
   */
  public Argument[] getArgumentsForGenMethod(InvokeExpr invokeExpr) {
    return getArgumentsFromMethodDescription(genSignatureToArgumentsMap, invokeExpr);
  }

  /**
   * Returns the arguments for a potential method that copies a COAL value. In this case the source
   * COAL value is the base of the method call. The value is copied to the return value of the
   * method.
   * 
   * @param invokeExpr An invoke expression.
   * @return An array of arguments if the invoke expression is for a copy modifier, null otherwise.
   */
  public Argument[] getArgumentsForCopyMethod(InvokeExpr invokeExpr) {
    return getArgumentsFromMethodDescription(copySignatureToArgumentsMap, invokeExpr);
  }

  /**
   * Returns the arguments for a potential copy constructor.
   * 
   * @param signature A method signature.
   * @return An array of arguments if the method signature is for a copy constructor of interest,
   *         null otherwise.
   */
  public Argument[] getArgumentsForCopyConstructor(String signature) {
    return copyConstructors.get(signature);
  }

  /**
   * Returns the arguments for a potential copy constructor.
   * 
   * @param methodRef A Soot method reference.
   * @return An array of arguments if the method reference is for a copy constructor of interest,
   *         null otherwise.
   */
  public Argument[] getArgumentsForCopyConstructor(SootMethodRef methodRef) {
    return getArgumentsForCopyConstructor(methodRef.getSignature());
  }

  /**
   * Returns the arguments for a potential COAL source.
   * 
   * @param invokeExpr An invoke expression.
   * @return An array of arguments if the invoke expression is for a COAL source, null otherwise.
   */
  public Argument[] getArgumentsForSource(InvokeExpr invokeExpr) {
    return getArgumentsFromMethodDescription(sourceSignatureToArgumentsMap, invokeExpr);
  }

  /**
   * Returns the arguments for a potential COAL query.
   * 
   * @param stmt A program statement.
   * @return An array of arguments if the statement is for a COAL query, null otherwise.
   */
  public Argument[] getArgumentsForQuery(Stmt stmt) {
    if (stmt.containsInvokeExpr()) {
      InvokeExpr invokeExpr = stmt.getInvokeExpr();
      SootMethod method = invokeExpr.getMethod();
      if (AnalysisParameters.v().isAnalysisClass(method.getDeclaringClass().getName())
          && method.isConcrete() && method.hasActiveBody()) {
        MethodDescription description = queryToMethodDescriptionMap.get(method.getSignature());
        if (description == null) {
          return null;
        } else {
          return description.getArguments();
        }
      }
      return getArgumentsFromMethodDescription(queryToMethodDescriptionMap, invokeExpr);
    }
    return null;
  }

  /**
   * Returns the arguments for a static final field. This is useful when COAL values are constants
   * that should not be propagated. They can be modeled using the COAL language and retrieved using
   * this method.
   * 
   * @param signature A field signature.
   * @return An array of arguments if the statement is for a COAL constant, null otherwise.
   */
  public Argument[] getArgumentsForStaticField(String signature) {
    return staticFieldToArgumentsMap.get(signature);
  }

  /**
   * Returns the arguments associated with a method descriptor.
   * 
   * @param signatureToMethodDescriptionMap A map from signatures to method descriptors.
   * @param invokeExpr An invoke expression.
   * @return An array of arguments if arguments are found for the method descriptor, null otherwise.
   */
  private Argument[] getArgumentsFromMethodDescription(
      Map<String, MethodDescription> signatureToMethodDescriptionMap, InvokeExpr invokeExpr) {
    SootMethod method = invokeExpr.getMethod();
    String signature = method.getSignature();
    MethodDescription methodDescription = signatureToMethodDescriptionMap.get(signature);
    if (methodDescription != null) {
      return methodDescription.getArguments();
    }
    signature = method.getSubSignature();
    methodDescription = signatureToMethodDescriptionMap.get(signature);
    if (methodDescription == null) {
      return null;
    }
    String superclassName = methodDescription.getBaseClass();
    if (superclassName == null || !Scene.v().containsClass(superclassName)
        || invokeExpr instanceof InterfaceInvokeExpr) {
      return null;
    }
    SootClass superclass = Scene.v().getSootClass(superclassName);
    String baseType;
    if (invokeExpr instanceof InstanceInvokeExpr) {
      Value baseValue = ((InstanceInvokeExpr) invokeExpr).getBase();
      baseType = baseValue.getType().toString();
    } else {
      baseType = invokeExpr.getMethod().getDeclaringClass().getName();
    }
    if (Scene.v().containsClass(baseType)
        && Scene.v().getActiveHierarchy()
            .isClassSubclassOfIncluding(Scene.v().getSootClass(baseType), superclass)) {
      return methodDescription.getArguments();
    } else {
      return null;
    }
  }

  /**
   * Determines if the class is excluded from the analysis results. This can be used when processing
   * the results results to avoid reporting results for a certain class. This does not prevent the
   * class from being traversed during the constant propagation, which is decided by whether the
   * class is included using the -input or -classpath flag on the command line.
   * 
   * @param name A fully-qualified class name.
   * @return True if the class should be excluded from the result.
   */
  public boolean isExcludedClass(String name) {
    return excludedClasses.contains(name);
  }

  /**
   * Adds a type to the set of modeled types.
   * 
   * @param type A fully-qualified class type.
   */
  public void addModeledType(String type) {
    modeledTypes.add(type);
  }

  /**
   * Adds a COAL modifier.
   * 
   * @param extendedSignature An {@link ExtendedSignature}.
   * @param arguments An array of arguments.
   * @param modifierModifier A modifier modifier.
   */
  public void addModifier(ExtendedSignature extendedSignature, Argument[] arguments,
      String modifierModifier) {
    MethodDescription methodDescription =
        new MethodDescription(extendedSignature.getSuperclass(), arguments);
    String signature = extendedSignature.getSignature();
    if (modifierModifier == null) {
      signatureToArgumentsMap.put(signature, methodDescription);
    } else if (modifierModifier.equals("gen")) {
      genSignatureToArgumentsMap.put(signature, methodDescription);
    } else if (modifierModifier.equals("copy")) {
      copySignatureToArgumentsMap.put(signature, methodDescription);
    } else {
      throw new RuntimeException("Unknown modifier modifier: " + modifierModifier);
    }
  }

  /**
   * Adds a COAL source.
   * 
   * @param extendedSignature An {@link ExtendedSignature}.
   * @param arguments An array of arguments.
   */
  public void addSource(ExtendedSignature extendedSignature, Argument[] arguments) {
    MethodDescription methodDescription =
        new MethodDescription(extendedSignature.getSuperclass(), arguments);
    sourceSignatureToArgumentsMap.put(extendedSignature.getSignature(), methodDescription);
  }

  /**
   * Adds a copy constructor.
   * 
   * @param signature A method signature.
   * @param arguments An array of arguments.
   */
  public void addCopyConstructor(String signature, Argument[] arguments) {
    copyConstructors.put(signature, arguments);
  }

  /**
   * Adds a COAL query.
   * 
   * @param extendedSignature An {@link ExtendedSignature}.
   * @param arguments An array of arguments.
   */
  public void addQuery(ExtendedSignature extendedSignature, Argument[] arguments) {
    MethodDescription methodDescription =
        new MethodDescription(extendedSignature.getSuperclass(), arguments);
    queryToMethodDescriptionMap.put(extendedSignature.getSignature(), methodDescription);
  }

  /**
   * Adds a COAL constant.
   * 
   * @param signature A field signature. This should be for a static final field (i.e., a constant).
   * @param arguments An array of arguments.
   */
  public void addConstant(String signature, Argument[] arguments) {
    staticFieldToArgumentsMap.put(signature, arguments);
  }

  /**
   * Adds a type for which results should not be reported.
   * 
   * @param name A fully-qualified class type.
   */
  public void addExcludedClass(String name) {
    excludedClasses.add(name);
  }

  /**
   * Ends the initialization of this model. This should be called after the model is populated. No
   * other modification to the model should be performed after calling this method.
   */
  public void endInitialization() {
    modeledTypes = Collections.unmodifiableSet(modeledTypes);
    signatureToArgumentsMap = Collections.unmodifiableMap(signatureToArgumentsMap);
    genSignatureToArgumentsMap = Collections.unmodifiableMap(genSignatureToArgumentsMap);
    copySignatureToArgumentsMap = Collections.unmodifiableMap(copySignatureToArgumentsMap);
    sourceSignatureToArgumentsMap = Collections.unmodifiableMap(sourceSignatureToArgumentsMap);
    copyConstructors = Collections.unmodifiableMap(copyConstructors);
    staticFieldToArgumentsMap = Collections.unmodifiableMap(staticFieldToArgumentsMap);
    queryToMethodDescriptionMap = Collections.unmodifiableMap(queryToMethodDescriptionMap);
    excludedClasses = Collections.unmodifiableSet(excludedClasses);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("*****Model*****\n");
    stringBuilder.append("**Modeled Types**\n");
    stringBuilder.append(modeledTypes).append("\n");

    stringBuilder.append("\n**Modifiers**\n");
    for (Map.Entry<String, MethodDescription> entry : signatureToArgumentsMap.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      stringBuilder.append(entry.getValue()).append("\n");
    }

    stringBuilder.append("\n**Generating Modifiers**\n");
    for (Map.Entry<String, MethodDescription> entry : genSignatureToArgumentsMap.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      stringBuilder.append(entry.getValue()).append("\n");
    }

    stringBuilder.append("\n**Copy Modifiers**\n");
    for (Map.Entry<String, MethodDescription> entry : copySignatureToArgumentsMap.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      stringBuilder.append(entry.getValue()).append("\n");
    }

    stringBuilder.append("\n**Sources**\n");
    for (Map.Entry<String, MethodDescription> entry : sourceSignatureToArgumentsMap.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      stringBuilder.append(entry.getValue()).append("\n");
    }

    stringBuilder.append("\n**Copy Constructors**\n");
    for (Map.Entry<String, Argument[]> entry : copyConstructors.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      for (Argument argument : entry.getValue()) {
        stringBuilder.append("    " + argument.toString()).append("\n");
      }
    }

    stringBuilder.append("\n**Static Fields**\n");
    for (Map.Entry<String, Argument[]> entry : staticFieldToArgumentsMap.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      for (Argument argument : entry.getValue()) {
        stringBuilder.append("    " + argument.toString()).append("\n");
      }
    }

    stringBuilder.append("\n**Queries**\n");
    for (Map.Entry<String, MethodDescription> entry : queryToMethodDescriptionMap.entrySet()) {
      stringBuilder.append(entry.getKey()).append("\n");
      stringBuilder.append(entry.getValue().toString()).append("\n");
    }

    stringBuilder.append("\n**Excluded Classes**\n");
    stringBuilder.append(excludedClasses).append("\n");

    return stringBuilder.toString();
  }

  public void dump() {
    System.out.println(toString());
  }

  private Model() {
  }
}
