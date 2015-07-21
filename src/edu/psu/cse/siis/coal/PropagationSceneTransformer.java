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

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import edu.psu.cse.siis.coal.arguments.StringValueAnalysis;

/**
 * The scene transformer for the propagation problem.
 */
public class PropagationSceneTransformer extends SceneTransformer {
  private static final int MAX_ITERATIONS = 15;

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ResultBuilder resultBuilder;
  private final PropagationSceneTransformerPrinter printer;

  /**
   * Constructor for the scene transformer.
   * 
   * @param resultBuilder A {@link ResultBuilder} that describes how the result is generated once
   *          the problem solution is found.
   * @param printer A {@link PropagationSceneTransformerPrinter} that prints the output of the
   *          analysis when debugging is enabled. If null, nothing gets printed.
   */
  public PropagationSceneTransformer(ResultBuilder resultBuilder,
      PropagationSceneTransformerPrinter printer) {
    this.resultBuilder = resultBuilder;
    this.printer = printer;
  }

  @Override
  protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
    PropagationTimers.v().totalTimer.start();
    PropagationTimers.v().misc.start();
    StringValueAnalysis.initialize();

    JimpleBasedInterproceduralCFG iCfg = new PropagationIcfg();
    AnalysisParameters.v().setIcfg(iCfg);
    PropagationProblem problem = new PropagationProblem(iCfg);
    for (SootMethod ep : Scene.v().getEntryPoints()) {
      if (ep.isConcrete()) {
        problem.getInitialSeeds().add(ep.getActiveBody().getUnits().getFirst());
      }
    }

    int iterationCounter = 0;
    PropagationSolver solver = null;

    while (iterationCounter < MAX_ITERATIONS) {
      IterationSolver.v().initialize(solver);
      PropagationTimers.v().misc.end();

      PropagationTimers.v().problemGeneration.start();
      solver = new PropagationSolver(problem);
      PropagationTimers.v().problemGeneration.end();

      PropagationTimers.v().ideSolution.start();
      logger.info("Solving propagation problem (iteration " + iterationCounter + ")");
      solver.solve();
      PropagationTimers.v().ideSolution.end();

      PropagationTimers.v().misc.start();
      if (!AnalysisParameters.v().isIterative() || IterationSolver.v().hasFoundFixedPoint()) {
        iterationCounter = MAX_ITERATIONS;
      } else {
        ++iterationCounter;
      }
    }
    PropagationTimers.v().misc.end();

    logger.info("Reached a fixed point");

    Results.addResult(resultBuilder.buildResult(solver));

    PropagationTimers.v().totalTimer.end();

    if (logger.isDebugEnabled()) {
      CallGraph cg = Scene.v().getCallGraph();

      Iterator<Edge> it = cg.listener();
      while (it.hasNext()) {
        soot.jimple.toolkits.callgraph.Edge e = (soot.jimple.toolkits.callgraph.Edge) it.next();
        logger.debug("" + e.src() + e.srcStmt() + " =" + e.kind() + "=> " + e.tgt());
      }

      if (printer != null) {
        printer.print(solver);
      }
    }
  }
}
