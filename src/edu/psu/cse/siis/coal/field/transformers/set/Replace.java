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
package edu.psu.cse.siis.coal.field.transformers.set;

import java.util.HashSet;
import java.util.Set;

/**
 * A field transformer for replace operations.
 * 
 * It is possible to replace set values with either a single value or a set of values.
 */
public class Replace extends SetFieldTransformer {
  @SuppressWarnings("unchecked")
  public Replace(Object value) {
    this.clear = true;
    this.add = new HashSet<>(2);
    if (value instanceof Set) {
      this.add.addAll((Set<Object>) value);
    } else {
      this.add.add(value);
    }
  }
}
