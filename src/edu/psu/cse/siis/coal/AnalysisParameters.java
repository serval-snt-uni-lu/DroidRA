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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

/**
 * Singleton with global analysis parameters.
 */
public class AnalysisParameters {
  private static AnalysisParameters instance = new AnalysisParameters();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Set<String> analysisClasses;
  private boolean iterative = true;
  private JimpleBasedInterproceduralCFG icfg;
  private boolean inferNonModeledTypes = true;
  private boolean useShimple = false;

  /**
   * Adds classes to the set of analysis classes. The analysis classes are the set of classes
   * through which constant propagation should take place.
   * 
   * @param classes A collection of classes.
   */
  public void addAnalysisClasses(Collection<String> classes) {
    if (analysisClasses == null) {
      analysisClasses = new HashSet<>();
    }

    analysisClasses.addAll(classes);
  }

  /**
   * Determines if a class is an analysis class.
   * 
   * @param clazz A fully-qualified class name.
   * @return True if the argument class is an analysis class.
   */
  public boolean isAnalysisClass(String clazz) {
    if (analysisClasses == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("No analysis classes set. To change this, use "
            + "Analysis.v().addAnalysisClasses()");
      }
      return false;
    }

    return analysisClasses.contains(clazz);
  }

  /**
   * Gets the set of analysis classes.
   * 
   * @return The set of analysis classes.
   */
  public Set<String> getAnalysisClasses() {
    return analysisClasses;
  }

  /**
   * Determines if the analysis is iterative. Currently all analyses should be iterative. The
   * default value is true.
   * 
   * @return True if the analysis is iterative.
   */
  public boolean isIterative() {
    return iterative;
  }

  /**
   * Sets the interprocedural control flow graph for the analysis.
   * 
   * @param icfg The interprocedural CFG.
   */
  public void setIcfg(JimpleBasedInterproceduralCFG icfg) {
    this.icfg = icfg;
  }

  /**
   * Gets the interprocedural control flow graph for the analysis.
   * 
   * @return The interprocedural control flow graph.
   */
  public JimpleBasedInterproceduralCFG getIcfg() {
    return icfg;
  }

  /**
   * Sets whether hotspot values should be inferred for values that are not modeled using COAL. The
   * COAL language allows method arguments with primitive values (string, int, etc.) to be specified
   * as hotspots. This flags specifies if such values should be inferred. Note that in order to
   * infer these values, argument analyses have to be specified for them using
   * {@link edu.psu.cse.siis.coal.arguments.ArgumentValueManager#registerArgumentValueAnalysis
   * registerArgumentValueAnalysis}. The default value is true.
   * 
   * @param inferNonModeledTypes The value of the flag.
   */
  public void setInferNonModeledTypes(boolean inferNonModeledTypes) {
    this.inferNonModeledTypes = inferNonModeledTypes;
  }

  /**
   * Determines whether hotspot values should be inferred for values that are not modeled using
   * COAL.
   * 
   * @return True if hotspot values should be inferred for values that are not modeled using COAL.
   */
  public boolean inferNonModeledTypes() {
    return inferNonModeledTypes;
  }

  /**
   * Determines if the analysis should be performed using Shimple (SSA representation).
   * 
   * @return True if the analysis should be performed using Shimple.
   */
  public boolean useShimple() {
    return useShimple;
  }

  /**
   * Returns the singleton instance for this class.
   * 
   * @return The singleton instance for this class.
   */
  public static AnalysisParameters v() {
    return instance;
  }

  private AnalysisParameters() {
  }
}
