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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import edu.psu.cse.siis.coal.values.BasePropagationValue;

/**
 * A {@link PropagationSceneTransformerPrinter} that prints to a file.
 */
public class PropagationSceneTransformerFilePrinter implements PropagationSceneTransformerPrinter {
  private final String filePath;
  private final SymbolFilter filter;

  /**
   * Constructor for the printer.
   * 
   * @param filePath The path to the output file.
   * @param filter A filter to filter out unwanted symbols (e.g., variables or fields whose value
   *          should not be printed).
   */
  public PropagationSceneTransformerFilePrinter(String filePath, SymbolFilter filter) {
    this.filePath = filePath;
    this.filter = filter;
  }

  @Override
  public void print(PropagationSolver solver) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
      String newLine = System.getProperty("line.separator");
      List<MethodOrMethodContext> eps =
          new ArrayList<MethodOrMethodContext>(Scene.v().getEntryPoints());
      ReachableMethods reachableMethods =
          new ReachableMethods(Scene.v().getCallGraph(), eps.iterator(), null);
      reachableMethods.update();
      for (Iterator<MethodOrMethodContext> iter = reachableMethods.listener(); iter.hasNext();) {
        SootMethod ep = iter.next().method();
        if (!ep.isConcrete() || !ep.hasActiveBody()
        // || ep.getName().contains("dummy")
        /* || Model.v().isExcludedClass(ep.getDeclaringClass().getName()) */) {
          continue;
        }
        writer.write(ep.getActiveBody() + newLine);

        writer.write("----------------------------------------------" + newLine);
        writer.write("At end of: " + ep.getSignature() + newLine);
        writer.write("Variables:" + newLine);
        writer.write("----------------------------------------------" + newLine);

        for (Unit ret : ep.getActiveBody().getUnits()) {

          for (Map.Entry<Value, BasePropagationValue> l : solver.resultsAt(ret).entrySet()) {
            Value symbol = l.getKey();
            if (filter.filterOut(symbol)) {
              continue;
            }
            writer.write(symbol + " contains value " + l.getValue() + newLine);
          }

          writer.write("**" + ret + newLine);
        }
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
