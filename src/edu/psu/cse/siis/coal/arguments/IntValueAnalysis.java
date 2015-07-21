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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Scene;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.LongConstant;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.scalar.Pair;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Constants;

/**
 * An argument value analysis for integer types.
 */
public class IntValueAnalysis extends BackwardValueAnalysis {
  private static final int TOP_VALUE = Constants.ANY_INT;

  @Override
  public Set<Object> computeInlineArgumentValues(String[] inlineValues) {
    Set<Object> result = new HashSet<>(inlineValues.length);

    for (String intString : inlineValues) {
      result.add(Integer.parseInt(intString));
    }

    return result;
  }

  /**
   * Returns the possible values for an integer variable.
   * 
   * @param value The variable whose value we are looking for.
   * @param start The statement where the variable is used.
   * @return The set of possible values for the variable.
   */
  @Override
  public Set<Object> computeVariableValues(Value value, Stmt start) {
    if (value instanceof IntConstant) {
      return Collections.singleton((Object) ((IntConstant) value).value);
    } else if (value instanceof LongConstant) {
      return Collections.singleton((Object) ((LongConstant) value).value);
    } else if (value instanceof Local) {
      return findIntAssignmentsForLocal(start, (Local) value, new HashSet<Stmt>());
    } else {
      return Collections.singleton((Object) TOP_VALUE);
    }
  }

  /**
   * Return all possible values for an integer local variable.
   * 
   * @param start The statement where the analysis should start.
   * @param local The local variable whose values we are looking for.
   * @param visitedStmts The set of visited statement.
   * @return The set of possible values for the local variable.
   */
  private Set<Object> findIntAssignmentsForLocal(Stmt start, Local local, Set<Stmt> visitedStmts) {
    List<DefinitionStmt> assignStmts =
        findAssignmentsForLocal(start, local, true, new HashSet<Pair<Unit, Local>>());
    Set<Object> result = new HashSet<>(assignStmts.size());

    for (DefinitionStmt assignStmt : assignStmts) {
      Value rhsValue = assignStmt.getRightOp();
      if (rhsValue instanceof IntConstant) {
        result.add(((IntConstant) rhsValue).value);
      } else if (rhsValue instanceof LongConstant) {
        result.add(((LongConstant) rhsValue).value);
      } else if (rhsValue instanceof ParameterRef) {
        ParameterRef parameterRef = (ParameterRef) rhsValue;
        Iterator<Edge> edges =
            Scene.v().getCallGraph()
                .edgesInto(AnalysisParameters.v().getIcfg().getMethodOf(assignStmt));
        while (edges.hasNext()) {
          Edge edge = edges.next();
          InvokeExpr invokeExpr = edge.srcStmt().getInvokeExpr();
          Value argValue = invokeExpr.getArg(parameterRef.getIndex());
          if (argValue instanceof IntConstant) {
            result.add(((IntConstant) argValue).value);
          } else if (argValue instanceof LongConstant) {
            result.add(((LongConstant) argValue).value);
          } else if (argValue instanceof Local) {
            Set<Object> newResults =
                findIntAssignmentsForLocal(edge.srcStmt(), (Local) argValue, visitedStmts);
            result.addAll(newResults);
          } else {
            result.add(TOP_VALUE);
          }
        }
      } else {
        return Collections.singleton((Object) TOP_VALUE);
      }
    }

    return result;
  }

  @Override
  public Object getTopValue() {
    return TOP_VALUE;
  }

}
