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
package edu.psu.cse.siis.coal.values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.cse.siis.coal.Constants;
import edu.psu.cse.siis.coal.Internable;
import edu.psu.cse.siis.coal.Pool;
import edu.psu.cse.siis.coal.PropagationSolver;
import edu.psu.cse.siis.coal.field.values.FieldValue;
import edu.psu.cse.siis.coal.field.values.IntermediateFieldValue;
import edu.psu.cse.siis.coal.field.values.NullFieldValue;

/**
 * A COAL propagation value, which is simply a collection of {@link PathValue} elements.
 */
public class PropagationValue implements BasePropagationValue, Internable<PropagationValue> {
  private static final Pool<PropagationValue> POOL = new Pool<>();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Set<PathValue> pathValues = new HashSet<>();

  /**
   * Returns the path values contained in this object.
   * 
   * @return The path values contained in this object.
   * 
   * @see PathValue
   */
  public Set<PathValue> getPathValues() {
    return this.pathValues;
  }

  /**
   * Adds a {@link PathValue} to this propagation value.
   * 
   * @param pathValue A PathValue.
   */
  public void addPathValue(PathValue pathValue) {
    this.pathValues.add(pathValue);
  }

  /**
   * Returns all the possible values for a given field, across all different paths.
   * 
   * @param field The name of a field.
   * @return The possible values of the field.
   */
  public Set<FieldValue> getValuesForField(String field) {
    Set<FieldValue> result = new HashSet<>();
    for (PathValue pathValue : this.pathValues) {
      if (pathValue == NullPathValue.v()) {
        FieldValue nullFieldValue = NullFieldValue.v();
        result.add(nullFieldValue);
      } else {
        result.add(pathValue.getFieldMap().get(field));
      }
    }

    return result;
  }

  /**
   * Joins this value with another one.
   * 
   * @param other A {@link PropagationValue}.
   * @return The resulting join.
   */
  public BasePropagationValue joinWith(PropagationValue other) {
    if (this.pathValues.size() + other.pathValues.size() > Constants.VALUE_LIMIT) {
      return TopPropagationValue.v();
    }

    PropagationValue result = new PropagationValue();
    result.pathValues.addAll(this.pathValues);
    result.pathValues.addAll(other.pathValues);

    return result.intern();
  }

  /**
   * Determines if this is an intermediate value, that is, if it contains any
   * {@link IntermediateFieldValue} element.
   * 
   * @return True if this is an intermediate value.
   */
  public boolean isIntermediateValue() {
    for (PathValue pathValue : pathValues) {
      if (pathValue.containsIntermediateField()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Computes a non-intermediate value from this value.
   * 
   * @param solver A propagation solver.
   */
  public void makeFinalValue(PropagationSolver solver) {
    if (!isIntermediateValue()) {
      return;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Making final value for " + this.toString());
    }
    Set<PathValue> finalPathValues = new HashSet<>();

    for (PathValue pathValue : this.pathValues) {
      if (pathValue.containsIntermediateField()) {
        finalPathValues.addAll(pathValue.makeFinalBranchValues(solver));
      } else {
        finalPathValues.add(pathValue);
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Transformed to " + finalPathValues.toString());
    }
    this.pathValues = finalPathValues;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Value: " + this.pathValues.size() + " path values\n");
    List<String> parts = new ArrayList<>(this.pathValues.size());
    for (PathValue pathValue : this.pathValues) {
      parts.add("  " + pathValue.toString() + "\n");
    }
    Collections.sort(parts);

    for (String part : parts) {
      result.append(part);
    }

    return result.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.pathValues);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof PropagationValue
        && Objects.equals(this.pathValues, ((PropagationValue) other).pathValues);
  }

  @Override
  public PropagationValue intern() {
    return POOL.intern(this);
  }
}
