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
package edu.psu.cse.siis.coal.transformers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.psu.cse.siis.coal.Internable;
import edu.psu.cse.siis.coal.Pool;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;
import edu.psu.cse.siis.coal.field.values.FieldValue;
import edu.psu.cse.siis.coal.field.values.NullFieldValue;
import edu.psu.cse.siis.coal.values.PathValue;

/**
 * A path transformer, which models the influence of a single path. It is a collection of field
 * transformers.
 */
public class PathTransformer implements Internable<PathTransformer> {
  private static final Pool<PathTransformer> POOL = new Pool<>();

  protected Map<String, FieldTransformer> fieldMap;

  public PathTransformer() {
    this.fieldMap = new HashMap<>();
  }

  /**
   * Adds a {@link FieldTransformer} to this path transformer.
   * 
   * @param field The name of the field for which a transformer is being added.
   * @param transformer The field transformer to be added.
   */
  public void addFieldTransformer(String field, FieldTransformer transformer) {
    this.fieldMap.put(field, transformer);
  }

  /**
   * Computes the result of applying this path transformer to a given {@link PathValue}.
   * 
   * @param source The source PathValue to which this path transformer should be applied.
   * @return The resulting PathValue.
   */
  public PathValue computeTarget(PathValue source) {
    Map<String, FieldValue> secondFieldMap = source.getFieldMap();
    PathValue result = new PathValue();

    for (Map.Entry<String, FieldTransformer> entry : this.fieldMap.entrySet()) {
      String field = entry.getKey();
      FieldTransformer fieldTransformer = entry.getValue();
      FieldValue fieldValue = secondFieldMap.get(field);

      if (fieldValue == null) {
        fieldValue = NullFieldValue.v();
      }

      if (fieldTransformer != null) {
        result.addFieldEntry(field, fieldTransformer.apply(fieldValue));
      } else {
        result.addFieldEntry(field, fieldValue);
      }
    }

    for (Map.Entry<String, FieldValue> entry : secondFieldMap.entrySet()) {
      String field = entry.getKey();
      if (!this.fieldMap.containsKey(field)) {
        result.addFieldEntry(field, entry.getValue());
      }
    }

    return result;
  }

  /**
   * Composes this path transformer with another one.
   * 
   * @param secondPathTransformer A path transformer.
   * @return The result of the composition of this path transformer with the argument.
   */
  public PathTransformer compose(PathTransformer secondPathTransformer) {
    if (secondPathTransformer instanceof NullPathTransformer) {
      return secondPathTransformer;
    }

    PathTransformer result = new PathTransformer();
    Map<String, FieldTransformer> secondFieldMap = secondPathTransformer.fieldMap;

    // A: set of fields in this transformer, B: set of fields in the second
    // transformer.
    // We compute A \ B and A & B.
    for (Map.Entry<String, FieldTransformer> entry : fieldMap.entrySet()) {
      String field = entry.getKey();
      FieldTransformer fieldTransformer = entry.getValue();
      if (fieldTransformer != null) {
        FieldTransformer secondFieldTransformer = secondFieldMap.get(field);
        if (secondFieldTransformer != null) {
          fieldTransformer = fieldTransformer.compose(secondFieldTransformer);
        }
        result.addFieldTransformer(field, fieldTransformer);
      }
    }

    // We compute B \ A.
    for (Map.Entry<String, FieldTransformer> entry : secondFieldMap.entrySet()) {
      String field = entry.getKey();
      if (this.fieldMap.containsKey(field)) {
        continue;
      }
      result.addFieldTransformer(field, entry.getValue());
    }

    return result.intern();
  }

  @Override
  public String toString() {
    return fieldMap != null ? fieldMap.toString() : "null";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.fieldMap);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof PathTransformer
        && Objects.equals(this.fieldMap, ((PathTransformer) other).fieldMap);
  }

  @Override
  public PathTransformer intern() {
    return POOL.intern(this);
  }
}
