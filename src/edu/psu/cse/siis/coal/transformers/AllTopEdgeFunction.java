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

import heros.EdgeFunction;
import heros.edgefunc.AllBottom;
import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.BottomPropagationValue;

/**
 * A customized "all top" edge function as required by the IDE solver. This does not actually have
 * "all top" semantics, we customized it to make it work with the IDE solver in a way compatible
 * with our propagation values.
 */
public class AllTopEdgeFunction extends AllBottom<BasePropagationValue> {
  public AllTopEdgeFunction() {
    super(BottomPropagationValue.v());
  }

  @Override
  public BasePropagationValue computeTarget(BasePropagationValue source) {
    return source;
  }

  @Override
  public EdgeFunction<BasePropagationValue> joinWith(
      EdgeFunction<BasePropagationValue> otherFunction) {
    return otherFunction;
  }
}
