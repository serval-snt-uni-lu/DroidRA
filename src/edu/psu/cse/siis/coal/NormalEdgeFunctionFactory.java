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

import heros.EdgeFunction;
import heros.edgefunc.EdgeIdentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.PointsToAnalysis;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.transformers.PropagationTransformerFactory;
import edu.psu.cse.siis.coal.values.BasePropagationValue;

/**
 * A factory for normal edge functions. Normal edge functions indicate how values are propagated
 * through normal (non-call) statements.
 */
public class NormalEdgeFunctionFactory {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Returns a normal edge function.
   * 
   * @param curr The current statement.
   * @param currNode The current variable.
   * @param succNode The variable the current variable is propagated to after the statement.
   * @param zeroValue The zero value, which represents the absence of a data flow fact.
   * @param pointsToAnalysis The pointer analysis.
   * @return A normal edge function.
   */
  public EdgeFunction<BasePropagationValue> getNormalEdgeFunction(Unit curr, Value currNode,
      Value succNode, Value zeroValue, PointsToAnalysis pointsToAnalysis) {
    if (curr instanceof AssignStmt) {
      if (logger.isDebugEnabled()) {
        logger.debug("Normal edge: " + curr);
        logger.debug(currNode + " " + succNode);
      }
      AssignStmt assignStmt = (AssignStmt) curr;

      final Value left = assignStmt.getLeftOp();
      final String type = left.getType().toString();
      final Value right = assignStmt.getRightOp();

      if (Model.v().isModeledType(type)) {
        if (currNode.equivTo(zeroValue) && succNode.equivTo(left)) {
          if (right instanceof StaticFieldRef) {
            StaticFieldRef staticFieldRef = (StaticFieldRef) right;

            Argument[] arguments =
                Model.v().getArgumentsForStaticField(staticFieldRef.getField().getSignature());

            EdgeFunction<BasePropagationValue> result =
                PropagationTransformerFactory.makeTransformer(null, arguments, false);
            if (arguments != null) {
              if (logger.isDebugEnabled()) {
                logger.debug("Returning " + result);
              }
              return PropagationTransformerFactory.makeTransformer(null, arguments, false);
            }
          } else if (right instanceof NullConstant) {
            return PropagationTransformerFactory.makeTransformer(null, null, false);
          }
        }
      }
    }
    return EdgeIdentity.v();
  }
}
