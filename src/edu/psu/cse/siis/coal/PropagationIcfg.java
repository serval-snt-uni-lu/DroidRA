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

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

/**
 * An interprocedural control flow graph where call graph edges to class initializers are included.
 */
public class PropagationIcfg extends JimpleBasedInterproceduralCFG {
  @Override
  public boolean isCallStmt(Unit unit) {
    Stmt stmt = (Stmt) unit;
    if (stmt.containsInvokeExpr()) {
      return true;
    } else if (stmt instanceof AssignStmt) {
      Value right = ((AssignStmt) stmt).getRightOp();
      return right instanceof StaticFieldRef
          && AnalysisParameters.v().isAnalysisClass(
              ((StaticFieldRef) right).getField().getDeclaringClass().getName());
    } else {
      return false;
    }
  }
}
