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

/**
 * An "all top" function with "all top" semantics in a join-semilattice. That is, map any element to
 * "top" and join to "all top".
 * 
 * @param <V> A COAL value type parameter.
 */
public class AllTop<V> extends heros.edgefunc.AllTop<V> {
  public AllTop(V topElement) {
    super(topElement);
  }

  @Override
  public EdgeFunction<V> composeWith(EdgeFunction<V> secondFunction) {
    return this;
  }

  @Override
  public EdgeFunction<V> joinWith(EdgeFunction<V> otherFunction) {
    return this;
  }
}
