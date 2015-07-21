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
package edu.psu.cse.siis.coal.arguments;

import java.io.Serializable;

/**
 * The representation of a field, which includes a name and a type.
 */
public class Field implements Serializable {
  private static final long serialVersionUID = 1L;
  private String name;
  private String type;

  /**
   * Constructor.
   * 
   * @param name The field name.
   * @param type The field type, which should be one of the types registered using
   *          {@link ArgumentValueManager#registerArgumentValueAnalysis}.
   */
  public Field(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return name + " (" + type + ")";
  }
}
