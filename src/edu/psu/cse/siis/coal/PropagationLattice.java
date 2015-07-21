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
package edu.psu.cse.siis.coal;

import heros.JoinLattice;
import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.BottomPropagationValue;
import edu.psu.cse.siis.coal.values.PropagationValue;
import edu.psu.cse.siis.coal.values.TopPropagationValue;

/**
 * The lattice for the MVMF constant propagation problem.
 */
public class PropagationLattice implements JoinLattice<BasePropagationValue> {
  @Override
  public BasePropagationValue topElement() {
    return BottomPropagationValue.v();
  }

  @Override
  public BasePropagationValue bottomElement() {
    return TopPropagationValue.v();
  }

  @Override
  public BasePropagationValue join(BasePropagationValue left, BasePropagationValue right) {
    // In the case of a method which is considered an entry point and has a modeled object
    // as an argument, as an "unassigned" argument it is assigned value top.
    // If another method calls it, then top will prevail if joining with top yields top.
    if (left instanceof BottomPropagationValue || left instanceof TopPropagationValue) {
      return right;
    }
    if (right instanceof BottomPropagationValue || right instanceof TopPropagationValue) {
      return left;
    }

    return ((PropagationValue) left).joinWith((PropagationValue) right);
    // Actually, it is possible to join different types for methods such as:
    // <java.util.ArrayList: boolean add(java.lang.Object)>
    // public boolean add(java.lang.Object) {
    // java.util.ArrayList this;
    // java.lang.Object object;
    // java.lang.RuntimeException $r0;
    //
    // this := @this: java.util.ArrayList;
    // object := @parameter0: java.lang.Object;
    // $r0 = new java.lang.RuntimeException;
    // specialinvoke $r0.<java.lang.RuntimeException: void
    // <init>(java.lang.String)>("Stub!");
    // throw $r0;
    // }
  }
}
