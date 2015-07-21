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

import heros.FlowFunction;
import heros.flowfunc.KillAll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

/**
 * A factory for call flow functions. Call flow functions determine how variables should be
 * propagated through call statements.
 */
public class CallFlowFunctionFactory {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Returns a call flow function.
   * 
   * @param src A statement that is the source of a call edge in the call graph. This is generally a
   *          call statement, but field accesses can also lead to edges leading to class
   *          initializers.
   * @param dest The destination method.
   * @param zeroValue The zero value for the analysis, which represents the absence of a data flow
   *          fact.
   * @return The call flow function for the input statement.
   */
  public FlowFunction<Value> getCallFlowFunction(Unit src, final SootMethod dest,
      final Value zeroValue) {
    if (logger.isDebugEnabled()) {
      logger.debug("Call: " + src);
    }

    String declaringClass = dest.getDeclaringClass().getName();

    if (!AnalysisParameters.v().isAnalysisClass(declaringClass)) {
      // Only propagate through analysis classes.
      return KillAll.v();
    }

    Stmt stmt = (Stmt) src;
    // Some statements other than call statements (e.g., field accesses) can lead to call edges to
    // class initializers.
    boolean containsInvokeExpr = stmt.containsInvokeExpr();

    final InvokeExpr ie = containsInvokeExpr ? stmt.getInvokeExpr() : null;

    if (containsInvokeExpr
        && (Model.v().getArgumentsForGenMethod(ie) != null || Model.v()
            .getArgumentsForCopyConstructor(ie.getMethodRef()) != null)) {
      return KillAll.v();
    }

    return new FlowFunction<Value>() {
      @Override
      public Set<Value> computeTargets(Value source) {
        if (logger.isDebugEnabled()) {
          logger.debug("Source: " + source);
        }

        if (dest.getName().equals(SootMethod.staticInitializerName)) {
          if (source instanceof FieldRef) {
            return Collections.singleton(source);
          } else {
            return Collections.emptySet();
          }
        }

        final List<Value> paramLocals = new ArrayList<Value>();

        for (int i = 0; i < dest.getParameterCount(); ++i) {
          // TODO (Damien): maybe activate again?
          // if (ie.getArg(i) instanceof NullConstant && source.equals(zeroValue)) {
          // return Collections.singleton((Value) dest.getActiveBody().getParameterLocal(i));
          // }
          paramLocals.add(dest.getActiveBody().getParameterLocal(i));
        }

        int argIndex = FunctionFactoryUtils.shouldPropagateSource(source, ie.getArgs());
        if (argIndex != -1) {
          if (logger.isDebugEnabled()) {
            logger.debug("Returning " + paramLocals.get(argIndex));
          }
          return Collections.singleton(paramLocals.get(argIndex));
        }

        if (source instanceof StaticFieldRef) {
          // Always propagate static fields.
          return Collections.singleton(source);
        } else if (source instanceof InstanceFieldRef) {
          if (FunctionFactoryUtils.shouldPropagateInstanceField((InstanceFieldRef) source, ie)) {
            return Collections.singleton(source);
          }
        }

        if (logger.isDebugEnabled()) {
          logger.debug("Returning empty set");
        }
        return Collections.emptySet();
      }
    };
  }
}
