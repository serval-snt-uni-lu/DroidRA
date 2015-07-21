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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
 * A sequence of {@link SequenceElement} that represents the influence of several consecutive COAL
 * value compositions.
 */
public class TransformerSequence {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private List<SequenceElement> transformerSequence = null;

  public TransformerSequence() {
  }

  public TransformerSequence(TransformerSequence otherTransformerSequence) {
    this.transformerSequence = new ArrayList<>(otherTransformerSequence.transformerSequence);
  }

  public TransformerSequence(List<SequenceElement> otherTransformerSequence) {
    this.transformerSequence = new ArrayList<>(otherTransformerSequence);
  }

  /**
   * Adds a transformer to the sequence. The transformer is added to the last element.
   * 
   * @param newFieldTransformer A field transformer.
   */
  public void addTransformerToSequence(FieldTransformer newFieldTransformer) {
    this.transformerSequence.get(this.transformerSequence.size() - 1).composeWith(
        newFieldTransformer);
  }

  /**
   * Concatenate another sequence to this sequence.
   * 
   * @param transformerSequence A transformer sequence.
   */
  public void addElementsToSequence(TransformerSequence transformerSequence) {
    if (this.transformerSequence == null) {
      this.transformerSequence = new ArrayList<>();
    }
    this.transformerSequence.addAll(transformerSequence.transformerSequence);
  }

  /**
   * Adds an element to the sequence.
   * 
   * @param symbol A symbol (variable) whose type is modeled with COAL.
   * @param stmt A modifier that references a variable modeled with COAL.
   * @param op An operation.
   */
  public void addElementToSequence(Value symbol, Stmt stmt, String op) {
    if (this.transformerSequence == null) {
      this.transformerSequence = new ArrayList<>();
    }
    this.transformerSequence.add(new SequenceElement(symbol, stmt, op));
  }

  /**
   * Generates the field transformers that represent the influence of this sequence.
   * 
   * @param field A field name.
   * @param solver A propagation solver.
   * @return The set of field transformers that represent the influence of this sequence.
   */
  public Set<FieldTransformer> makeFinalFieldTransformers(String field, PropagationSolver solver) {
    Set<FieldTransformer> result =
        Collections.singleton((FieldTransformer) IdentityFieldTransformer.v());
    if (logger.isDebugEnabled()) {
      logger.debug("Making final field transformers for " + this);
    }
    for (SequenceElement sequenceElement : transformerSequence) {
      result =
          composeFieldTransformerSets(result, sequenceElement.makeFinalTransformers(field, solver));
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Returning " + result);
    }
    return result;
  }

  /**
   * Composes two sets of field transformers.
   * 
   * @param fieldTransformers1 A set of field transformers.
   * @param fieldTransformers2 Another set of field transformers.
   * @return The result of the composition.
   */
  private Set<FieldTransformer> composeFieldTransformerSets(
      Set<FieldTransformer> fieldTransformers1, Set<FieldTransformer> fieldTransformers2) {
    Set<FieldTransformer> result = new HashSet<>();
    for (FieldTransformer fieldTransformer1 : fieldTransformers1) {
      for (FieldTransformer fieldTransformer2 : fieldTransformers2) {
        if (fieldTransformer1 != null && fieldTransformer2 != null) {
          result.add(fieldTransformer1.compose(fieldTransformer2));
        } else {
          result.add(null);
        }
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return this.transformerSequence.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.transformerSequence);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TransformerSequence)) {
      return false;
    }
    TransformerSequence secondTransformerSequence = (TransformerSequence) other;
    return Objects.equals(this.transformerSequence, secondTransformerSequence.transformerSequence);
  }
}
