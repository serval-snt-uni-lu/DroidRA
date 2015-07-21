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
package edu.psu.cse.siis.coal.arguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Unit;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Constants;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformerManager;

/**
 * A singleton manager for method argument values, which trigger argument value analyses.
 */
public class ArgumentValueManager {
  private static final short MAX_TIME = 300;
  private static final short MIN_TIME = 120;
  private static final float DECREASE_FACTOR = 0.90f;

  private static final ArgumentValueManager instance = new ArgumentValueManager();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Map<String, ArgumentValueAnalysis> argumentValueAnalysisMap = new HashMap<>();
  private final Map<ArgumentValueIdentifier, Set<Object>> cachedValues = new HashMap<>();
  private final Map<String, FieldTransformer> topFieldTransformerMap = new HashMap<>();

  private short timeBudget = MAX_TIME;

  private ArgumentValueManager() {
  }

  public static ArgumentValueManager v() {
    return instance;
  }

  /**
   * Registers the argument value analysis associated with a given type.
   * 
   * @param type An argument value type.
   * @param analysis An analysis for the argument value type.
   */
  public void registerArgumentValueAnalysis(String type, ArgumentValueAnalysis analysis) {
    argumentValueAnalysisMap.put(type, analysis);
  }

  /**
   * Returns the argument value analysis associated with a given type.
   * 
   * @param type An argument value type.
   * @return An argument value analysis.
   */
  public ArgumentValueAnalysis getArgumentValueAnalysis(String type) {
    ArgumentValueAnalysis analysis = argumentValueAnalysisMap.get(type);
    if (analysis == null) {
      throw new RuntimeException("No analysis for type " + type);
    }

    return analysis;
  }

  /**
   * Registers default argument value analyses, which are provided as part of the vanilla COAL
   * solver.
   */
  public void registerDefaultArgumentValueAnalyses() {
    registerArgumentValueAnalysis(Constants.DefaultArgumentTypes.Scalar.STRING,
        new StringValueAnalysis());
    registerArgumentValueAnalysis(Constants.DefaultArgumentTypes.Scalar.CLASS,
        new ClassValueAnalysis());
    registerArgumentValueAnalysis(Constants.DefaultArgumentTypes.Scalar.INT, new IntValueAnalysis());
    registerArgumentValueAnalysis(Constants.DefaultArgumentTypes.Set.STRING,
        new StringValueAnalysis());
    registerArgumentValueAnalysis(Constants.DefaultArgumentTypes.Set.CLASS,
        new ClassValueAnalysis());
    registerArgumentValueAnalysis(Constants.DefaultArgumentTypes.Set.INT, new IntValueAnalysis());
  }

  /**
   * Returns the possible argument values for a given program statement and a given {@link Argument}
   * .
   * 
   * @param argument An argument.
   * @param callSite A call statement.
   * @return The possible argument values.
   */
  public Set<Object> getArgumentValues(Argument argument, Unit callSite) {
    String type = argument.getType();
    if (type == null) {
      return null;
    }

    if (AnalysisParameters.v().isIterative()) {
      ArgumentValueIdentifier argumentValueIdentifier =
          new ArgumentValueIdentifier(callSite, argument);
      Set<Object> result = cachedValues.get(argumentValueIdentifier);
      if (result != null) {
        return result;
      } else {
        result = computeNewArgumentValues(argument, callSite);
        cachedValues.put(argumentValueIdentifier, result);
        return result;
      }
    } else {
      return computeNewArgumentValues(argument, callSite);
    }
  }

  /**
   * Returns a field transformer that indicates an unknown field value.
   * 
   * @param type An argument type.
   * @return A field transformer.
   */
  public FieldTransformer getTopFieldTransformer(String type, String operation) {
    String key = new StringBuilder(type).append("::").append(operation).toString();
    FieldTransformer fieldTransformer = topFieldTransformerMap.get(key);
    if (fieldTransformer == null) {
      fieldTransformer = makeTopFieldTransformer(type, operation);
      topFieldTransformerMap.put(key, fieldTransformer);
    }
    return fieldTransformer;
  }

  /**
   * Generates a field transformer that indicates an unknown value for a given field type.
   * 
   * @param type A field type.
   * @return An field transformer.
   */
  private FieldTransformer makeTopFieldTransformer(String type, String operation) {
    ArgumentValueAnalysis analysis = getArgumentValueAnalysis(type);

    return FieldTransformerManager.v().makeFieldTransformer(operation, analysis.getTopValue());
  }

  /**
   * Returns possible arguments values for a given program statement and a given {@link Argument}
   * (helper function).
   * 
   * @param argument An argument.
   * @param callSite A call statement.
   * @return The possible argument values.
   */
  private Set<Object> computeNewArgumentValues(Argument argument, Unit callSite) {
    String type = argument.getType();
    String[] inlineValues = argument.getInlineValues();

    ArgumentValueAnalysis analysis = getArgumentValueAnalysis(type);

    if (inlineValues == null) {
      ExecutorService pool = Executors.newFixedThreadPool(1);
      // long start = System.currentTimeMillis();
      Future<Set<Object>> valuesFuture =
          startComputingNewArgumentValues(pool, analysis, argument, callSite);

      try {
        return valuesFuture.get(timeBudget, TimeUnit.SECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException exc) {
        valuesFuture.cancel(true);
        synchronized (this) {
          if (timeBudget > MIN_TIME) {
            timeBudget *= DECREASE_FACTOR;
            if (timeBudget < MIN_TIME) {
              timeBudget = MIN_TIME;
            }
          }
        }

        logger.warn("Could not infer argument value at statement " + callSite.toString(), exc);
        return Collections.singleton(analysis.getTopValue());
      } catch (Error error) {
        // Bad practice in general, but we don't control the analyses (especially the string
        // analysis, which is sometimes very greedy). So we still attempt to recover from this.
        valuesFuture.cancel(true);
        // OutOfMemoryError is a very likely candidate.
        System.gc();

        logger.warn("Could not infer argument value at statement " + callSite.toString(), error);
        return Collections.singleton(analysis.getTopValue());
      } finally {
        pool.shutdownNow();

        // int duration = (int) (System.currentTimeMillis() - start);
        // synchronized (PropagationTimers.v().argumentValueTime) {
        // PropagationTimers.v().argumentValueTime += duration;
        // }
      }
    } else {
      return analysis.computeInlineArgumentValues(inlineValues);
    }
  }

  /**
   * Generates a future for a new argument value computation.
   * 
   * @param analysis An argument value analysis.
   * @param argument An argument.
   * @param callSite A call statement.
   * @return A future for the new argument value computation.
   */
  private Future<Set<Object>> startComputingNewArgumentValues(final ExecutorService pool,
      final ArgumentValueAnalysis analysis, final Argument argument, final Unit callSite) {
    Callable<Set<Object>> callable = new Callable<Set<Object>>() {
      @Override
      public Set<Object> call() {
        return analysis.computeArgumentValues(argument, callSite);
      }
    };

    return pool.submit(callable);
  }
}
