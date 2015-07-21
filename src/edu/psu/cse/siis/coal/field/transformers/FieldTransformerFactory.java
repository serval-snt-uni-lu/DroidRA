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

import soot.Value;
import soot.jimple.Stmt;

/**
 * An abstract factory for field transformers. Subclasses are expected to override
 * {@link #makeFieldTransformer(Object)}.
 */
public abstract class FieldTransformerFactory {

  /**
   * Returns a field transformer for a given COAL argument value. Most subclasses should override
   * this method.
   * 
   * @param value An argument value.
   * @return A field transformer.
   */
  public FieldTransformer makeFieldTransformer(Object value) {
    throw new RuntimeException("makeFieldTransformer(value) not implemented in factory "
        + this.getClass().toString());
  }

  /**
   * Returns a field transformer for a given variable, a given statement and an operation.
   * 
   * At the moment, this is not used.
   * 
   * @param symbol A COAL symbol (variable).
   * @param stmt A COAL modifier.
   * @param op An operation to be performed on a field.
   * @return A field transformer.
   */
  public FieldTransformer makeFieldTransformer(Value symbol, Stmt stmt, String op) {
    throw new RuntimeException("makeFieldTransformer(symbol, stmt) not implemented in factory "
        + this.getClass().toString());
  }
}
