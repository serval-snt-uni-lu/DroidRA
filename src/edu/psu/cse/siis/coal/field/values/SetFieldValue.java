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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SetFieldValue extends FieldValue {
  private Set<Object> values;

  @Override
  public Object getValue() {
    return values;
  }

  /**
   * Adds a set of values to this field value.
   * 
   * @param add A set of values.
   */
  public void addAll(Set<Object> add) {
    if (add == null) {
      return;
    }
    if (this.values == null) {
      this.values = new HashSet<>(add.size());
    }
    this.values.addAll(add);
  }

  /**
   * Removes a set of values from this field value.
   * 
   * @param remove A set of values.
   */
  public void removeAll(Set<Object> remove) {
    // No need to do anything if there is no value.
    if (this.values != null) {
      this.values.removeAll(remove);
    }
  }

  @Override
  public String toString() {
    if (values == null) {
      return "null";
    }

    return Objects.toString(values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(values);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof SetFieldValue
        && Objects.equals(this.values, ((SetFieldValue) other).values);
  }

}
