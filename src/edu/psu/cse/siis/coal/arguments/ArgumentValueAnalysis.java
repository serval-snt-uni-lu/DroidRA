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

import java.util.Collections;
import java.util.Set;

import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Constants;

/**
 * An argument value analysis. Subclasses indicate how to analyze a certain argument type. They
 * should override:
 * <ul>
 * <li>{@link #computeVariableValues(Value, Stmt)} to compute the argument value at a given
 * statement for a given Value.</li>
 * <li>{@link #computeInlineArgumentValues(String[])} to compute an inline argument value from a
 * string representation.</li>
 * <li>{@link #getTopValue()} to indicate how an unknown argument value should be represented.</li>
 * </ul>
 * 
 * It is also possible to override {@link #computeArgumentValues(Argument, Unit)} if the argument
 * value computation requires access to the corresponding {@link Argument} object.
 */
public abstract class ArgumentValueAnalysis {

  /**
   * Computes the possible argument values for a given statement and a given argument.
   * 
   * By default this simply calls {@link #computeArgumentValues(Argument, Unit)}.
   * 
   * @param argument An {@link Argument}.
   * @param callSite A call statement.
   * @return The set of possible values for the argument.
   */
  public Set<Object> computeArgumentValues(Argument argument, Unit callSite) {
    if (argument.getArgnum() == null) {
      return null;
    }
    if (AnalysisParameters.v().useShimple()) {
      // Shimple is not supported.
      return Collections.singleton((Object) getTopValue());
    } else {
      Stmt stmt = (Stmt) callSite;
      if (!stmt.containsInvokeExpr()) {
        throw new RuntimeException("Statement " + stmt + " does not contain an invoke expression");
      }
      InvokeExpr invokeExpr = stmt.getInvokeExpr();
      int argnum = argument.getArgnum()[0];
      Value value = null;
      if (argnum == Constants.INSTANCE_INVOKE_BASE_INDEX) {
        if (invokeExpr instanceof InstanceInvokeExpr) {
          value = ((InstanceInvokeExpr) invokeExpr).getBase();
        } else {
          throw new RuntimeException("Invoke expression has no base: " + invokeExpr);
        }
      } else {
        value = stmt.getInvokeExpr().getArg(argnum);
      }

      return computeVariableValues(value, stmt);
    }
  }

  /**
   * Computes the possible values of a variable at a given statement.
   * 
   * @param value The variable for which possible values should be computed.
   * @param callSite A call statement.
   * @return The set of possible values for the variable.
   */
  public abstract Set<Object> computeVariableValues(Value value, Stmt callSite);

  /**
   * Computes a set of inline values from a string representation.
   * 
   * @param inlineValues An array of strings.
   * @return The set of inline values.
   */
  public abstract Set<Object> computeInlineArgumentValues(String[] inlineValues);

  /**
   * Returns the representation of an unknown argument value.
   * 
   * @return The representation of an unknown argument value.
   */
  public abstract Object getTopValue();

}
