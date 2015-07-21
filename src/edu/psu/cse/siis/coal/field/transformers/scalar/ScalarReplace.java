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
package edu.psu.cse.siis.coal.field.transformers.scalar;

import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;
import edu.psu.cse.siis.coal.field.values.FieldValue;
import edu.psu.cse.siis.coal.field.values.ScalarFieldValue;

public class ScalarReplace extends FieldTransformer {
  private final Object value;

  public ScalarReplace(Object newValue) {
    value = newValue;
  }

  @Override
  public FieldValue apply(FieldValue fieldValue) {
    return new ScalarFieldValue(value);
  }

  /**
   * Composes two scalar field operations.
   * 
   * There are only two possible operations for a scalar: replace or clear (make null). In both
   * cases, when we perform composition, only the second one remains.
   * 
   * @param secondFieldOperation The scalar operation with which this transformer is composed.
   * @return The resulting transformer.
   */
  @Override
  public FieldTransformer compose(FieldTransformer secondFieldOperation) {
    return secondFieldOperation;
  }
}
