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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.Pair;
import edu.psu.cse.siis.coal.AnalysisParameters;

/**
 * An abstract backward argument value analysis, which provides utility functions.
 */
public abstract class BackwardValueAnalysis extends ArgumentValueAnalysis {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Returns all assignments for a local variable. This walks the interprocedural control flow graph
   * back from a statement looking for all assignments to a given local variable.
   * 
   * @param start The statement where the analysis should start.
   * @param local The local variable whose assignments should be found.
   * @param init A boolean that indicates whether the analysis should be initialized. This should
   *          always be true for non-recursive calls.
   * @param visitedUnits The set of statements visited by the analysis.
   * @return The set of assignment statements for the local variable.
   */
  protected List<DefinitionStmt> findAssignmentsForLocal(Unit start, Local local, boolean init,
      Set<Pair<Unit, Local>> visitedUnits) {
    if (logger.isDebugEnabled()) {
      logger.debug("Finding assignments for local " + local);
    }
    SootMethod method = AnalysisParameters.v().getIcfg().getMethodOf(start);
    ExceptionalUnitGraph graph = new ExceptionalUnitGraph(method.getActiveBody());
    List<DefinitionStmt> result = new ArrayList<DefinitionStmt>();

    Stack<Unit> stack = new Stack<Unit>();
    stack.push(start);
    if (init) {
      visitedUnits.clear();
    }

    while (!stack.empty()) {
      Unit current = stack.pop();
      if (logger.isDebugEnabled()) {
        logger.debug(current + " " + current.getClass());
      }
      Pair<Unit, Local> pair = new Pair<Unit, Local>(current, local);
      if (visitedUnits.contains(pair)) {
        continue;
      }
      visitedUnits.add(pair);
      if (current instanceof IdentityStmt) {
        IdentityStmt identityStmt = (IdentityStmt) current;
        // method.
        if (identityStmt.getLeftOp().equivTo(local)) {
          result.add(identityStmt);
        }
      } else if (current instanceof AssignStmt) {
        AssignStmt assignStmt = (AssignStmt) current;
        if (assignStmt.getLeftOp().equivTo(local)) {
          if (assignStmt.getRightOp() instanceof Local) {
            result.addAll(findAssignmentsForLocal(current, (Local) assignStmt.getRightOp(), false,
                visitedUnits));
          } else {
            result.add(assignStmt);
          }
          // The assignment generates the local on that path.
          // Anything before is irrelevant.
          continue;
        }
      }
      for (Unit pred : graph.getPredsOf(current)) {
        stack.push(pred);
      }
    }

    return result;
  }

}
