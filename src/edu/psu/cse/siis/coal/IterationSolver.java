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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Unit;
import soot.Value;

import com.google.common.base.Objects;

import edu.psu.cse.siis.coal.arguments.ArgumentValueManager;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformerManager;
import edu.psu.cse.siis.coal.field.values.FieldValue;
import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.PropagationValue;
import edu.psu.cse.siis.coal.values.TopPropagationValue;

/**
 * Singleton for managing iterations of the IDE analysis.
 */
public class IterationSolver {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private PropagationSolver solver = null;
  private static IterationSolver instance = new IterationSolver();
  private Set<LocationIdentifier> currentTopValues;
  private Set<LocationIdentifier> previousTopValues;

  private static final class LocationIdentifier {
    private final Unit stmt;
    private final Value symbol;
    private final String field;
    private final String type;
    private final String operation;

    public LocationIdentifier(Unit stmt, Value symbol, String field, String type, String operation) {
      this.stmt = stmt;
      this.symbol = symbol;
      this.field = field;
      this.type = type;
      this.operation = operation;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.stmt, this.symbol, this.field, this.type, this.operation);
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof LocationIdentifier) {
        LocationIdentifier locationIdentifier = (LocationIdentifier) other;
        return Objects.equal(this.stmt, locationIdentifier.stmt)
            && Objects.equal(this.symbol, locationIdentifier.symbol)
            && Objects.equal(this.field, locationIdentifier.field)
            && Objects.equal(this.type, locationIdentifier.type)
            && Objects.equal(this.operation, locationIdentifier.operation);
      }
      return false;
    }
  }

  public static IterationSolver v() {
    return instance;
  }

  /**
   * Determines if a fixed point has been reached or there are no undetermined values.
   * 
   * @return True if a fixed point has been reached or there are no undetermined values.
   */
  public boolean hasFoundFixedPoint() {
    return currentTopValues.isEmpty() || currentTopValues.equals(previousTopValues);
  }

  /**
   * Initializes the iteration manager before an iteration. This should be called before every
   * iteration.
   * 
   * @param propagationSolver The solver from the previous iteration, or null if this is the first
   *          iteration.
   */
  public synchronized void initialize(PropagationSolver propagationSolver) {
    solver = propagationSolver;
    previousTopValues = currentTopValues;
    currentTopValues = new HashSet<>();
  }

  /**
   * Generates a set of field transformers that represent the influence of a referenced value. This
   * is used when the argument of a COAL modifier is a value that is itself modeled with COAL. In
   * this case, once the referenced value is known, we generate field transformers that take all the
   * possible values of the argument into account. The argument may have more than one value, which
   * is why the return type is a set.
   * 
   * @param stmt The call statement that references another COAL value.
   * @param symbol The variable (referenced COAL value) whose value should be determined.
   * @param field The field that the transformer should operate on.
   * @param type The type of the field.
   * @param operation The operation to be performed.
   * @return The set of field transformers that represent the influence of the referenced value.
   */
  public Set<FieldTransformer> makeTransformersFromReferencedValue(Unit stmt, Value symbol,
      String field, String type, String operation) {
    synchronized (this) {
      if (solver == null) {
        // This is the first iteration, return top.
        currentTopValues.add(new LocationIdentifier(stmt, symbol, field, type, operation));
        return Collections.singleton(ArgumentValueManager.v().getTopFieldTransformer(type,
            operation));
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Making transformer for " + symbol + " for " + field + " at \n" + stmt);
    }

    BasePropagationValue referencedBaseValue = solver.resultAt(stmt, symbol);
    if (referencedBaseValue == null || referencedBaseValue instanceof TopPropagationValue) {
      // This is not the first iteration, but we still got top.
      logger.info("Found top at " + stmt);
      synchronized (this) {
        currentTopValues.add(new LocationIdentifier(stmt, symbol, field, type, operation));
      }
      return Collections
          .singleton(ArgumentValueManager.v().getTopFieldTransformer(type, operation));
    }

    PropagationValue referencedPropagationValue = (PropagationValue) referencedBaseValue;

    Set<FieldValue> fieldValues = referencedPropagationValue.getValuesForField(field);
    Set<FieldTransformer> result = new HashSet<>();
    for (FieldValue fieldValue : fieldValues) {
      if (fieldValue != null) {
        result.add(FieldTransformerManager.v().makeFieldTransformer(operation,
            fieldValue.getValue()));
      } else {
        result.add(null);
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Returning " + result.toString());
    }

    return result;
  }
}
