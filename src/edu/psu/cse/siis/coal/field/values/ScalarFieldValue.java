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

import java.util.Objects;

public class ScalarFieldValue extends FieldValue {
  private final Object value;

  public ScalarFieldValue(Object value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue(Class<T> type) {
    if (value == null) {
      return null;
    }
    try {
      return (T) value;
    } catch (ClassCastException classCastException) {
      throw new RuntimeException("Could not convert field value " + value + " of type "
          + value.getClass() + " to type " + type);

    }
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Objects.toString(value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ScalarFieldValue
        && Objects.equals(this.value, ((ScalarFieldValue) other).value);
  }
}
