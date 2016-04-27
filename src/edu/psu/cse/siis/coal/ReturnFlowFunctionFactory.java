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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

/**
 * A factory for return flow functions. Return flow functions indicate how symbols (variables) are
 * propagation through method return statements.
 */
public class ReturnFlowFunctionFactory {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Returns a return flow function.
   * 
   * @param callSite A call statement.
   * @param callee The called method.
   * @param exitStmt The exit statement for the method.
   * @param retSite The statement to which the method returns.
   * @param zeroValue The zero value, which represents the absence of a data flow fact.
   * @return A return flow function.
   */
  public FlowFunction<Value> getReturnFlowFunction(final Unit callSite, SootMethod callee,
      Unit exitStmt, Unit retSite, final Value zeroValue) {
    Stmt stmt = (Stmt) callSite;
    if (logger.isDebugEnabled()) {
      logger.debug("Stmt: " + stmt);
    }

    if (SootMethod.staticInitializerName.equals(callee.getName())) {
      // Static initializer: return everything that was created inside the method.
      return new FlowFunction<Value>() {
        @Override
        public Set<Value> computeTargets(Value source) {
          if (logger.isDebugEnabled()) {
            logger.debug("source: " + source);
          }
          if (callSite instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) callSite;
            Value right = assignStmt.getRightOp();
            if (logger.isDebugEnabled()) {
              logger.debug("right: " + right);
            }
            if (right instanceof StaticFieldRef && right.toString().equals(source.toString())) {
              Set<Value> result = new HashSet<>();
              result.add(source);
              result.add(assignStmt.getLeftOp());
              if (logger.isDebugEnabled()) {
                logger.debug("Returning " + result);
              }
              return result;
            }
          } else if (source instanceof FieldRef) {
            if (logger.isDebugEnabled()) {
              logger.debug("Returning " + source);
            }
            return Collections.singleton(source);
          }
          if (logger.isDebugEnabled()) {
            logger.debug("Returning empty set");
          }
          return Collections.emptySet();
        }

      };
    }

    String declaringClass = callee.getDeclaringClass().getName();

    if (Model.v().getArgumentsForGenMethod(stmt.getInvokeExpr()) != null
        || Model.v().getArgumentsForCopyConstructor(stmt.getInvokeExpr().getMethodRef()) != null
        || !AnalysisParameters.v().isAnalysisClass(declaringClass)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Killing all");
      }
      return KillAll.v();
    }

    if (exitStmt instanceof ReturnStmt || exitStmt instanceof ReturnVoidStmt
        || exitStmt instanceof ThrowStmt) {
      final List<Value> paramLocals = new ArrayList<Value>();

      for (int i = 0; i < callee.getParameterCount(); ++i) {
        paramLocals.add(callee.getActiveBody().getParameterLocal(i));
      }

      if (callSite instanceof InvokeStmt) {
        InvokeStmt invokeStmt = (InvokeStmt) callSite;
        if (logger.isDebugEnabled()) {
          logger.debug("Invoke Stmt: " + invokeStmt);
        }
        final InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();

        return new FlowFunction<Value>() {
          @Override
          public Set<Value> computeTargets(Value source) {
            if (logger.isDebugEnabled()) {
              logger.debug("Invoke expr: " + invokeExpr + "\nSource: " + source);
            }
            for (int i = 0; i < paramLocals.size(); ++i) {
              if (paramLocals.get(i).equivTo(source)) {
                return Collections.singleton(invokeExpr.getArg(i));
              }
            }
            if (source instanceof FieldRef) {
              if (logger.isDebugEnabled()) {
                logger.debug("Detected and returning field ref");
              }
              return Collections.singleton(source);
            }

            if (logger.isDebugEnabled()) {
              logger.debug("Returning empty set");
            }
            return Collections.emptySet();
          }
        };
      } else if (callSite instanceof DefinitionStmt && !(exitStmt instanceof ThrowStmt)
          && !(exitStmt instanceof ReturnVoidStmt)) {
        // The condition !(exitStmt instanceof ReturnVoidStmt) is due to the fact that Soot
        // automatically creates call graph edges between calls to <android.os.Handler: boolean
        // postDelayed(java.lang.Runnable,long)> (and similar methods) and the argument Runnable's
        // run() method. So even though we have a DefinitionStmt, we may still have a callee with a
        // void return statement. It has no influence on the objects we are interested in, since the
        // return value of the post* functions is a boolean.
        if (logger.isDebugEnabled()) {
          logger.debug("Stmt: " + callSite);
        }
        DefinitionStmt defnStmt = (DefinitionStmt) callSite;
        final Value leftOp = defnStmt.getLeftOp();

        ReturnStmt returnStmt = (ReturnStmt) exitStmt;
        final Value retLocal = returnStmt.getOp();

        final InvokeExpr invokeExpr = (InvokeExpr) defnStmt.getRightOp();

        return new FlowFunction<Value>() {
          @Override
          public Set<Value> computeTargets(Value source) {
            if (logger.isDebugEnabled()) {
              logger.debug("source: " + source);
            }
            if (source.equivTo(retLocal)) {
              if (logger.isDebugEnabled()) {
                logger.debug("Returning " + Collections.singleton(leftOp));
              }
              return Collections.singleton(leftOp);
            }
            for (int i = 0; i < paramLocals.size(); ++i) {
              if (paramLocals.get(i).equivTo(source)) {
                if (logger.isDebugEnabled()) {
                  logger.debug("Returning " + Collections.singleton(invokeExpr.getArg(i)));
                }
                return Collections.singleton(invokeExpr.getArg(i));
              }
            }
            if (source instanceof FieldRef) {
              return Collections.singleton(source);
            }
            if (logger.isDebugEnabled()) {
              logger.debug("Returning " + Collections.emptySet());
            }
            return Collections.emptySet();
          }
        };
      }
    }
    return KillAll.v();
  }
}
