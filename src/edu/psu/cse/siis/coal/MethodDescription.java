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

import java.io.Serializable;

import edu.psu.cse.siis.coal.arguments.Argument;

/**
 * A method descriptor that includes the base class that declares this method and all the arguments
 * of interest. The purpose of declaring the base class is that we want to detect all overridden
 * versions of the method.
 */
public class MethodDescription implements Serializable {
  private static final long serialVersionUID = 1L;
  private String baseClass = null;
  private Argument[] arguments;

  public MethodDescription(String baseClass, Argument[] arguments) {
    this.baseClass = baseClass;
    this.arguments = arguments;
  }

  public String getBaseClass() {
    return baseClass;
  }

  public void setBaseClass(String baseClass) {
    this.baseClass = baseClass;
  }

  public Argument[] getArguments() {
    return arguments;
  }

  public void setArguments(Argument[] arguments) {
    this.arguments = arguments;
  }

  @Override
  public String toString() {
    StringBuilder result = null;
    if (null == baseClass) {
      result = new StringBuilder();
    }
    else {
      result = new StringBuilder(baseClass);
    }

    for (Argument argument : arguments) {
      result.append("    " + argument.toString());
    }

    return result.toString();
  }

}
