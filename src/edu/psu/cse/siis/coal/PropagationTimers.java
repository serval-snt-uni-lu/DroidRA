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

import soot.Timer;

/**
 * Timers and counters for the execution of the COAL solver.
 */
public class PropagationTimers {
  private static PropagationTimers instance = new PropagationTimers();

  private PropagationTimers() {
  }

  public static PropagationTimers v() {
    synchronized (instance) {
      return instance;
    }
  }

  public static void clear() {
    instance = new PropagationTimers();
  }

  public Timer modelParsing = new Timer("modelParsing");

  public Timer problemGeneration = new Timer("IDE problem generation");

  public Timer ideSolution = new Timer("ideSolution");

  public Timer constantAnalysis = new Timer("Constant analysis");

  public Timer misc = new Timer("Misc");

  public Timer valueComposition = new Timer("Value composition");

  public Timer resultGeneration = new Timer("Result generation");

  public Timer totalTimer = new Timer("totalTimer");

  public Timer soot = new Timer("soot");

  public int entryPoints = 0;

  public int reachableMethods = 0;

  public long reachableStatements = 0;

  public Integer argumentValueTime = 0;

  public int classes = 0;

  public int pathValues = 0;
}
