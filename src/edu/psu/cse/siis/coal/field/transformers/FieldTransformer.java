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
package edu.psu.cse.siis.coal.field.transformers;

import edu.psu.cse.siis.coal.Internable;
import edu.psu.cse.siis.coal.Pool;
import edu.psu.cse.siis.coal.field.values.FieldValue;

/**
 * A field transformer, which models the influence of a statement of a single field.
 */
public abstract class FieldTransformer implements Internable<FieldTransformer> {
  private static final Pool<FieldTransformer> POOL = new Pool<>();

  /**
   * Applies this field transformer to a {@link FieldValue}.
   * 
   * @param fieldValue A field value.
   * @return A field value.
   */
  public abstract FieldValue apply(FieldValue fieldValue);

  /**
   * Composes this field transformer with another one.
   * 
   * @param secondFieldOperation A field transformer.
   * @return The result of composing this field transformer with the argument transformer.
   */
  public abstract FieldTransformer compose(FieldTransformer secondFieldOperation);

  @Override
  public FieldTransformer intern() {
    return POOL.intern(this);
  }
}
