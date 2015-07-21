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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Pool<T> {
  private final Map<T, T> pool = Collections.synchronizedMap(new HashMap<T, T>());

  public T intern(T element) {
    synchronized (pool) {
      T poolT = pool.get(element);
      if (poolT != null) {
        return poolT;
      } else {
        pool.put(element, element);
        return element;
      }
    }
  }

  public Set<T> getValues() {
    return new HashSet<T>(pool.values());
  }
}
