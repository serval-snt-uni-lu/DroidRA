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
import heros.flowfunc.Identity;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Hierarchy;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.shimple.PhiExpr;
import soot.shimple.PiExpr;

/**
 * A factory for normal flow functions. Normal edge functions indicate how symbols (variables) are
 * propagated through normal (non-call) statements.
 */
public class NormalFlowFunctionFactory {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Returns a normal flow function.
   * 
   * @param src The source statement, whose influence will be modeled by the returned function.
   * @param dest The destination statement.
   * @param zeroValue The zero value, which represents the absence of a data flow fact.
   * @param pointsToAnalysis The pointer analysis.
   * @return A normal flow function.
   */
  public FlowFunction<Value> getNormalFlowFunction(final Unit src, Unit dest,
      final Value zeroValue, final PointsToAnalysis pointsToAnalysis) {
    if (src instanceof AssignStmt) {

      final AssignStmt definitionStmt = (AssignStmt) src;

      final Value left = definitionStmt.getLeftOp();
      final String type = left.getType().toString();

      if (Model.v().isModeledType(type)) {
        return new FlowFunction<Value>() {
          @Override
          public Set<Value> computeTargets(Value source) {
            if (logger.isDebugEnabled()) {
              logger.debug("Normal flow stmt: " + src);
            }
            Value right = definitionStmt.getRightOp();
            Set<Value> res = new HashSet<Value>();
            if (logger.isDebugEnabled()) {
              logger.debug("Source: " + source + " " + (source instanceof StaticFieldRef) + " "
                  + (right instanceof StaticFieldRef));
            }

            if (source.equivTo(zeroValue) && right instanceof StaticFieldRef) {
              StaticFieldRef rightFieldRef = (StaticFieldRef) right;

              if (Model.v().getArgumentsForStaticField(rightFieldRef.getField().getSignature()) != null) {
                if (logger.isDebugEnabled()) {
                  logger.debug("Adding constant");
                }
                res.add(left);
              }
            } else if (source.equivTo(zeroValue) && right instanceof NullConstant) {
              res.add(left);
            } else if (source instanceof InstanceFieldRef && right instanceof InstanceFieldRef) {
              InstanceFieldRef sourceFieldRef = (InstanceFieldRef) source;
              InstanceFieldRef rightFieldRef = (InstanceFieldRef) right;
              PointsToSet sourceBasePointsToSet =
                  pointsToAnalysis.reachingObjects((Local) sourceFieldRef.getBase());
              PointsToSet rightBasePointsToSet =
                  pointsToAnalysis.reachingObjects((Local) rightFieldRef.getBase());

              if (sourceBasePointsToSet.hasNonEmptyIntersection(rightBasePointsToSet)) {
                PointsToSet sourceFieldPointsToSet =
                    pointsToAnalysis.reachingObjects(sourceBasePointsToSet,
                        sourceFieldRef.getField());
                PointsToSet rightFieldPointsToSet =
                    pointsToAnalysis
                        .reachingObjects(rightBasePointsToSet, rightFieldRef.getField());
                // Some cases where a field ref was not detected were seen (see
                // application yong.app.yyvideoplayer).
                if (sourceFieldRef.getField().getSubSignature()
                    .equals(rightFieldRef.getField().getSubSignature())
                    || sourceFieldPointsToSet.hasNonEmptyIntersection(rightFieldPointsToSet)) {
                  res.add(left);
                }
              }
            } else if (source instanceof StaticFieldRef && right instanceof StaticFieldRef) {
              StaticFieldRef sourceFieldRef = (StaticFieldRef) source;
              StaticFieldRef rightFieldRef = (StaticFieldRef) right;

              PointsToSet sourcePointsToSet =
                  pointsToAnalysis.reachingObjects(sourceFieldRef.getField());
              PointsToSet rightPointsToSet =
                  pointsToAnalysis.reachingObjects(rightFieldRef.getField());

              if (source.toString().equals(right.toString())
                  || sourcePointsToSet.hasNonEmptyIntersection(rightPointsToSet)
                  || haveCommonFields(sourceFieldRef.getFieldRef().declaringClass(), rightFieldRef
                      .getFieldRef().declaringClass())) {
                res.add(left);
              }
            } else if (source.equivTo(right) || source.equals(zeroValue)
                || (right instanceof PhiExpr && ((PhiExpr) right).getValues().contains(source))
                || (right instanceof PiExpr && ((PiExpr) right).getValue().equals(source))) {
              res.add(left);
            } else if (right instanceof ArrayRef && source.equivTo(((ArrayRef) right).getBase())) {
              // Transfer array values to elements.
              // For example, for statement e = a[i], we transfer all values of a to e. This is not
              // very precise (we can add precision later if needed) but allows us to proceed.
              res.add(left);
            }

            if (source.equivTo(zeroValue)) {
              res.add(source);
            }

            if (!source.equivTo(left) && !source.equals(zeroValue)
                && !(source.toString().equals(left.toString()) && Model.v().isModeledType(type))) {
              res.add(source);
            }

            if (logger.isDebugEnabled()) {
              logger.debug("Returning " + res);
            }
            return res;
          }
        };
      }
    }
    return Identity.v();
  }

  /**
   * Determines if two classes have common fields. This includes interface types. For example, this
   * can happen when a class implements an interface or when it subclasses another class.
   * 
   * @param sootClass1 A Soot class.
   * @param sootClass2 Another Soot class.
   * @return True if the two classes have common fields.
   */
  public boolean haveCommonFields(SootClass sootClass1, SootClass sootClass2) {
    if (logger.isDebugEnabled()) {
      logger.debug(sootClass1 + "\n" + sootClass2);
    }
    boolean haveCommonFields = false;
    Hierarchy hierarchy = Scene.v().getActiveHierarchy();
    if (sootClass1.isInterface()) {
      if (sootClass2.isInterface()) {
        haveCommonFields =
            hierarchy.isInterfaceSubinterfaceOf(sootClass1, sootClass2)
                || hierarchy.isInterfaceSuperinterfaceOf(sootClass1, sootClass2);
        if (logger.isDebugEnabled()) {
          logger.debug("1: " + haveCommonFields);
        }
      } else {
        haveCommonFields = sootClass2.implementsInterface(sootClass1.getName());
        if (logger.isDebugEnabled()) {
          logger.debug("2: " + haveCommonFields);
        }
      }
    } else {
      if (sootClass2.isInterface()) {
        haveCommonFields = sootClass1.implementsInterface(sootClass2.getName());
        if (logger.isDebugEnabled()) {
          logger.debug("3: " + haveCommonFields);
        }
      } else {
        haveCommonFields =
            hierarchy.isClassSubclassOf(sootClass1, sootClass2)
                || hierarchy.isClassSuperclassOf(sootClass1, sootClass2);
        if (logger.isDebugEnabled()) {
          logger.debug("4: " + haveCommonFields);
        }
      }
    }

    return haveCommonFields;
  }
}
