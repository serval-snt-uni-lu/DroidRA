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

import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;

/**
 * A {@link FieldTransformer} for clear operations. This is a singleton, since all clear field
 * transformers have the same parameters.
 */
public class Clear extends SetFieldTransformer {
  private static final Clear instance = new Clear();

  public static Clear v() {
    return instance;
  }

  @Override
  public FieldTransformer compose(FieldTransformer secondFieldOperation) {
    if (secondFieldOperation instanceof Clear) {
      return this;
    } else {
      return super.compose(secondFieldOperation);
    }
  }

  private Clear() {
    this.clear = true;
  }
}
