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

import java.util.HashMap;
import java.util.Map;

import soot.Unit;
import edu.psu.cse.siis.coal.arguments.Argument;

/**
 * A constant propagation result.
 */
public class Result {
  private Map<Unit, Map<Integer, Object>> result = new HashMap<>();
  private String statistics;

  /**
   * Returns the results. A result is a map between statements and statement results. There may be
   * more than an argument that is of interest in a COAL hotspot. A statement result is a map
   * between argument index and value.
   * 
   * @return The results.
   */
  public Map<Unit, Map<Integer, Object>> getResults() {
    return result;
  }

  /**
   * Returns the result for a given statement and a given argument.
   * 
   * @param unit A statement.
   * @param argument An argument.
   * @return The result for the input statement and argument.
   */
  public Object getResult(Unit unit, Argument argument) {
    Map<Integer, Object> unitResult = result.get(unit);
    if (unitResult != null) {
      return unitResult.get(argument.getArgnum());
    }
    return null;
  }

  /**
   * Returns problem statistics in the form of a string.
   * 
   * @return Some statistics for the problem.
   */
  public String getStatistics() {
    return statistics;
  }

  /**
   * Adds a result.
   * 
   * @param unit A COAL hotspot.
   * @param argnum The argument index.
   * @param value The value of the argument.
   */
  public void addResult(Unit unit, int argnum, Object value) {
    Map<Integer, Object> unitResult = result.get(unit);
    if (unitResult == null) {
      unitResult = new HashMap<>();
      result.put(unit, unitResult);
    }
    unitResult.put(argnum, value);
  }

  /**
   * Sets statistics in the form of a string.
   * 
   * @param statistics The statistics.
   */
  public void setStatistics(String statistics) {
    this.statistics = statistics;
  }
}
