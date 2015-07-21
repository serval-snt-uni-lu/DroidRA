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

import soot.Value;
import soot.jimple.Stmt;

/**
 * A descriptor for a COAL source.
 */
public class SourceDescriptor {
  private final Value symbol;
  private final Stmt stmt;

  /**
   * Constructor.
   * 
   * @param symbol The symbol, which should be the base of a COAL source method call.
   * @param stmt The COAL source statement.
   */
  public SourceDescriptor(Value symbol, Stmt stmt) {
    this.symbol = symbol;
    this.stmt = stmt;
  }

  public Value getSymbol() {
    return symbol;
  }

  public Stmt getStmt() {
    return stmt;
  }

  @Override
  public String toString() {
    return "source descriptor: symbol=" + symbol + ", stmt=" + stmt;
  }
}
