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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.Value;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.ExceptionalUnitGraph;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Constants;
import edu.psu.cse.siis.coal.arguments.LanguageConstraints.Call;

/**
 * An argument value analysis for string types.
 */
public class StringValueAnalysis extends ArgumentValueAnalysis {
  private static final String TOP_VALUE = Constants.ANY_STRING;

  /**
   * Initializes the string argument value analysis. This should be called before using the
   * analysis.
   */
  public static void initialize() {
    // CCExprVisitor.verbose_level = 20;
    // CCVisitor.verbose_level = 20;
    // DBG.verbose_level = 20;
    ConstraintCollector.globalCollection(new ConstraintCollector.CCModelInterface() {
      public boolean isExcludedClass(String class_name) {
        return false;
      }
    });
  }

  @Override
  public Set<Object> computeInlineArgumentValues(String[] inlineValues) {
    return new HashSet<Object>(Arrays.asList(inlineValues));
  }

  /**
   * Returns the string values of a variable used at a given statement.
   * 
   * @param value The value or variable that should be determined.
   * @param stmt The statement that uses the variable whose values should be determined.
   * @return The set of possible values.
   */
  @Override
  public Set<Object> computeVariableValues(Value value, Stmt stmt) {
    if (value instanceof StringConstant) {
      return Collections.singleton((Object) ((StringConstant) value).value.intern());
    } else if (value instanceof NullConstant) {
      return Collections.singleton((Object) "<NULL>");
    } else if (value instanceof Local) {
      Local local = (Local) value;

      ConstraintCollector constraintCollector =
          new ConstraintCollector(new ExceptionalUnitGraph(AnalysisParameters.v().getIcfg()
              .getMethodOf(stmt).getActiveBody()));
      LanguageConstraints.Box lcb = constraintCollector.getConstraintOfAt(local, stmt);
      RecursiveDAGSolverVisitorLC dagvlc =
          new RecursiveDAGSolverVisitorLC(5, null,
              new RecursiveDAGSolverVisitorLC.MethodReturnValueAnalysisInterface() {
                @Override
                public Set<Object> getMethodReturnValues(Call call) {
                  return MethodReturnValueManager.v().getMethodReturnValues(call);
                }
              });

      if (dagvlc.solve(lcb)) {
        // boolean flag = false;
        // if (dagvlc.result.size() == 0 || flag == true) {
        // System.out.println("ID: " + lcb.uid);
        // // int dbg = 10;
        // // while (dbg == 10) {
        // System.out.println("Returning " + dagvlc.result);
        // System.out.println("Returning.. " + lcb);
        // dagvlc.solve(lcb);
        // System.out.println("done");
        // // }
        // }
        // System.out.println("Returning " + dagvlc.result);
        return new HashSet<Object>(dagvlc.result);
      } else {
        return Collections.singleton((Object) TOP_VALUE);
      }
    } else {
      return Collections.singleton((Object) TOP_VALUE);
    }
  }

  @Override
  public Object getTopValue() {
    return TOP_VALUE;
  }
}
