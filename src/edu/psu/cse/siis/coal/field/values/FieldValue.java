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

import edu.psu.cse.siis.coal.Internable;
import edu.psu.cse.siis.coal.Pool;

/**
 * A COAL field value. It is modeled as a set of objects that could be anything, depending on the
 * problem.
 */
public abstract class FieldValue implements Internable<FieldValue> {
  private static final Pool<FieldValue> POOL = new Pool<>();

  /**
   * Returns the value represented by this field value.
   * 
   * @return The value represented by this field value.
   */
  public abstract Object getValue();

  /**
   * Determines if the field value makes a reference to another COAL value. In other words, this
   * determines if the field value is not completely resolved.
   * 
   * @return True if the field value makes a reference to another COAL value.
   */
  public boolean hasTransformerSequence() {
    return false;
  }

  @Override
  public FieldValue intern() {
    return POOL.intern(this);
  }
}
