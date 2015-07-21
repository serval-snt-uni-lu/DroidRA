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
package edu.psu.cse.siis.coal.field;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Value;
import soot.jimple.Stmt;
import edu.psu.cse.siis.coal.PropagationSolver;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;
import edu.psu.cse.siis.coal.field.transformers.IdentityFieldTransformer;

/**
 * A element that represents the influence of a modifier after a value composition. This is not be
 * used in the iterative version of the algorithm. After we encounter a modifier that makes a
 * reference to another COAL value, we record further transformers for the same field in this
 * element.
 */
public class SequenceElement {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Value symbol;
  private final Stmt stmt;
  private final String op;
  private FieldTransformer fieldTransformer;

  public SequenceElement(Value symbol, Stmt stmt, String op) {
    this.symbol = symbol;
    this.stmt = stmt;
    this.op = op;
    this.fieldTransformer = IdentityFieldTransformer.v();
  }

  public void composeWith(FieldTransformer newFieldTransformer) {
    this.fieldTransformer = this.fieldTransformer.compose(newFieldTransformer);
  }

  /**
   * Generates transformers that represent the influence of this element, including any field
   * transformer that was encountered after the COAL value composition.
   * 
   * @param field The field that is modified.
   * @param solver A propagation solver.
   * @return The set of field transformers that represent this element.
   */
  public Set<FieldTransformer> makeFinalTransformers(String field, PropagationSolver solver) {
    Set<FieldTransformer> result = new HashSet<>();
    for (FieldTransformer currentFieldTransformer : makeTransformersFromReferencedValue(field,
        solver)) {
      if (currentFieldTransformer != null) {
        result.add(currentFieldTransformer.compose(this.fieldTransformer));
      } else {
        result.add(null);
      }
    }

    return result;
  }

  /**
   * Generates field transformers for a referenced value represented by this element. There may be
   * more than one transformer for a single referenced variable, since the referenced variable may
   * have more than one possible value.
   * 
   * @param field A field name.
   * @param solver A propagation solver.
   * @return A set of field transformers that represent the influence of the referenced value.
   */
  private Set<FieldTransformer> makeTransformersFromReferencedValue(String field,
      PropagationSolver solver) {
    if (logger.isDebugEnabled()) {
      logger.debug("Making transformer from element " + this.toString());
    }

    throw new RuntimeException("Not implemented in this version.");
    // return FieldTransformerUtils.makeTransformersFromReferencedValue(stmt, symbol, field, null,
    // solver, op, new Boolean[1]);
  }

  @Override
  public String toString() {
    return "symbol " + this.symbol + ", stmt " + this.stmt + ", op " + this.op
        + ", field transformer " + this.fieldTransformer;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.symbol, this.stmt, this.op, this.fieldTransformer);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SequenceElement)) {
      return false;
    }
    SequenceElement secondSequenceElement = (SequenceElement) other;
    return Objects.equals(this.symbol, secondSequenceElement.symbol)
        && Objects.equals(this.stmt, secondSequenceElement.stmt)
        && Objects.equals(this.op, secondSequenceElement.op)
        && Objects.equals(this.fieldTransformer, secondSequenceElement.fieldTransformer);
  }
}
