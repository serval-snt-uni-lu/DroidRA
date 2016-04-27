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

import heros.DefaultSeeds;
import heros.EdgeFunction;
import heros.EdgeFunctions;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.JoinLattice;
import heros.edgefunc.EdgeIdentity;
import heros.template.DefaultIDETabulationProblem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.NullType;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import edu.psu.cse.siis.coal.transformers.AllTopEdgeFunction;
import edu.psu.cse.siis.coal.values.BasePropagationValue;

/**
 * Definition of the IDE problem for MVMF constant propagation.
 */
public class PropagationProblem
    extends
    DefaultIDETabulationProblem<Unit, Value, SootMethod, BasePropagationValue, JimpleBasedInterproceduralCFG> {
  public static final EdgeFunction<BasePropagationValue> ALL_TOP = new AllTopEdgeFunction();

  private final Set<Unit> initialSeeds = new HashSet<Unit>();
  private final PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();
  private JimpleBasedInterproceduralCFG icfg;

  /*
   * Edge and flow function factories.
   */
  private NormalEdgeFunctionFactory normalEdgeFunctionFactory = new NormalEdgeFunctionFactory();
  private NormalFlowFunctionFactory normalFlowFunctionFactory = new NormalFlowFunctionFactory();
  private CallFlowFunctionFactory callFlowFunctionFactory = new CallFlowFunctionFactory();
  private CallToReturnEdgeFunctionFactory callToReturnEdgeFunctionFactory =
      new CallToReturnEdgeFunctionFactory();
  private CallToReturnFlowFunctionFactory callToReturnFlowFunctionFactory =
      new CallToReturnFlowFunctionFactory();
  private ReturnFlowFunctionFactory returnFlowFunctionFactory = new ReturnFlowFunctionFactory();

  public PropagationProblem(JimpleBasedInterproceduralCFG icfg) {
    super(icfg);
    this.icfg = icfg;
  }

  public Set<Unit> getInitialSeeds() {
    return initialSeeds;
  }

  @Override
  protected JoinLattice<BasePropagationValue> createJoinLattice() {
    return new PropagationLattice();
  }

  /**
   * Factory for edge functions.
   * 
   * @return The edge functions.
   */
  @Override
  protected EdgeFunctions<Unit, Value, SootMethod, BasePropagationValue>
      createEdgeFunctionsFactory() {
    return new EdgeFunctions<Unit, Value, SootMethod, BasePropagationValue>() {

      @Override
      public EdgeFunction<BasePropagationValue> getNormalEdgeFunction(Unit curr, Value currNode,
          Unit succ, Value succNode) {
        return normalEdgeFunctionFactory.getNormalEdgeFunction(curr, currNode, succNode,
            zeroValue(), pointsToAnalysis);
      }

      @Override
      public EdgeFunction<BasePropagationValue> getCallEdgeFunction(Unit callStmt, Value srcNode,
          SootMethod destinationMethod, Value destNode) {
        // TODO (Damien): maybe activate again?
        // InvokeExpr invokeExpr = ((Stmt) callStmt).getInvokeExpr();
        //
        // for (int i = 0; i < destinationMethod.getParameterCount(); ++i) {
        // if (invokeExpr.getArg(i) instanceof NullConstant && srcNode.equals(zeroValue())
        // && destNode.equals(destinationMethod.getActiveBody().getParameterLocal(i))) {
        // PropagationTransformer propagationTransformer = new PropagationTransformer();
        // propagationTransformer.addBranchTransformer(NullBranchTransformer.v());
        // }
        // }

        return EdgeIdentity.v();
      }

      @Override
      public EdgeFunction<BasePropagationValue> getReturnEdgeFunction(Unit callSite,
          SootMethod calleeMethod, Unit exitStmt, Value exitNode, Unit returnSite, Value retNode) {
        return EdgeIdentity.v();
      }

      @Override
      public EdgeFunction<BasePropagationValue> getCallToReturnEdgeFunction(Unit callSite,
          Value callNode, Unit returnSite, Value returnSideNode) {
        return callToReturnEdgeFunctionFactory.getCallToReturnEdgeFunction(callSite, callNode,
            returnSite, returnSideNode, pointsToAnalysis);
      }
    };
  }

  /**
   * Factory for flow functions.
   * 
   * @return The flow functions.
   */
  @Override
  protected FlowFunctions<Unit, Value, SootMethod> createFlowFunctionsFactory() {
    return new FlowFunctions<Unit, Value, SootMethod>() {

      @Override
      public FlowFunction<Value> getNormalFlowFunction(Unit src, Unit dest) {
        return normalFlowFunctionFactory.getNormalFlowFunction(src, dest, zeroValue(),
            pointsToAnalysis);
      }

      @Override
      public FlowFunction<Value> getCallFlowFunction(Unit src, final SootMethod dest) {
        return callFlowFunctionFactory.getCallFlowFunction(src, dest, zeroValue());
      }

      @Override
      public FlowFunction<Value> getReturnFlowFunction(Unit callSite, SootMethod callee,
          Unit exitStmt, Unit retSite) {
        return returnFlowFunctionFactory.getReturnFlowFunction(callSite, callee, exitStmt, retSite,
            zeroValue());
      }

      @Override
      public FlowFunction<Value> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
        return callToReturnFlowFunctionFactory.getCallToReturnFlowFunction(call, returnSite,
            zeroValue(), icfg);
      }
    };
  }

  @Override
  public Value createZeroValue() {
    return new JimpleLocal("zero", NullType.v());
  }

  @Override
  public Map<Unit, Set<Value>> initialSeeds() {
    return DefaultSeeds.make(initialSeeds, zeroValue());
  }

  @Override
  protected EdgeFunction<BasePropagationValue> createAllTopFunction() {
    return ALL_TOP;
  }

  @Override
  public int numThreads() {
    return AnalysisParameters.v().getThreadCount();
  }
}
