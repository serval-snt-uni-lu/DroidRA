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
package edu.psu.cse.siis.coal.field.values;

import java.util.Objects;
import java.util.Set;

import edu.psu.cse.siis.coal.PropagationSolver;
import edu.psu.cse.siis.coal.field.TransformerSequence;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;

/**
 * An intermediate {@link FieldValue}, which represents a field value that references another COAL
 * value.
 */
public class IntermediateFieldValue extends FieldValue {
  private TransformerSequence transformerSequence = null;

  /**
   * Returns the {@link TransformerSequence} that models the influence of referenced values that are
   * also modeled with COAL, as well as subsequent COAL modifiers that modify the same field.
   * 
   * @return The transformer sequence that represents the influence of referenced COAL values and
   *         any subsequent COAL modifiers that modify the same field.
   */
  public TransformerSequence getTransformerSequence() {
    return this.transformerSequence;
  }

  @Override
  public boolean hasTransformerSequence() {
    return this.transformerSequence != null;
  }

  /**
   * Adds a {@link FieldTransformer} to the {@link TransformerSequence} included in this field
   * value.
   * 
   * @param newFieldTransformer A field transformer.
   */
  public void addTransformerToSequence(FieldTransformer newFieldTransformer) {
    if (this.transformerSequence == null) {
      throw new RuntimeException("Cannot add transformer to empty sequence.");
    }
    this.transformerSequence.addTransformerToSequence(newFieldTransformer);
  }

  /**
   * Concatenates a {@link TransformerSequence} to the TransformerSequence contained in this field
   * value.
   * 
   * @param transformerSequence A transformer sequence.
   */
  public void addTransformerSequence(TransformerSequence transformerSequence) {
    if (this.transformerSequence == null) {
      this.transformerSequence = new TransformerSequence();
    }
    this.transformerSequence.addElementsToSequence(transformerSequence);
  }

  /**
   * Generates field values without {@link TransformerSequence} from this intermediate field value.
   * This resolves the referenced values and integrates them into this field value.
   * 
   * @param field A field name.
   * @param solver A propagation solver.
   * @return The equivalent field values without references to other COAL field values.
   */
  public Set<FieldValue> makeFinalFieldValues(String field, PropagationSolver solver) {
    throw new RuntimeException("Not implemented for this version yet!");

    // Set<FieldValue> result = new HashSet<>();
    //
    // Set<FieldTransformer> sequenceTransformers =
    // this.transformerSequence.makeFinalFieldTransformers(field, solver);
    // for (FieldTransformer sequenceTransformer : sequenceTransformers) {
    // // This copy constructor does not copy the transformer sequence.
    // FieldValue currentFieldValue = new FieldValue(this);
    // if (sequenceTransformer != null) {
    // result.add(sequenceTransformer.apply(currentFieldValue));
    // } else {
    // result.add(currentFieldValue);
    // }
    // }
    //
    // return result;
  }

  @Override
  public String toString() {
    return "values " + this.getValue() + ", transformer sequence " + this.transformerSequence;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue(), transformerSequence);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof IntermediateFieldValue)) {
      return false;
    }
    IntermediateFieldValue secondFieldValue = (IntermediateFieldValue) other;
    return Objects.equals(this.getValue(), secondFieldValue.getValue())
        && Objects.equals(this.transformerSequence, secondFieldValue.transformerSequence);
  }

  @Override
  public Object getValue() {
    throw new RuntimeException("Not implemented");
  }
}
