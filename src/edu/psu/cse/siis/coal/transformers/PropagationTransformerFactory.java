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
package edu.psu.cse.siis.coal.transformers;

import heros.EdgeFunction;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Unit;
import soot.jimple.Stmt;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Constants;
import edu.psu.cse.siis.coal.IterationSolver;
import edu.psu.cse.siis.coal.Model;
import edu.psu.cse.siis.coal.arguments.Argument;
import edu.psu.cse.siis.coal.arguments.ArgumentValueManager;
import edu.psu.cse.siis.coal.arguments.SourceDescriptor;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformer;
import edu.psu.cse.siis.coal.field.transformers.FieldTransformerManager;
import edu.psu.cse.siis.coal.field.transformers.IdentityFieldTransformer;
import edu.psu.cse.siis.coal.values.BasePropagationValue;

/**
 * A factory for edge functions.
 */
public class PropagationTransformerFactory {
  private static final Logger logger = LoggerFactory.getLogger(PropagationTransformerFactory.class);

  /**
   * Returns an edge function for a given modifier and given arguments.
   * 
   * @param callSite The modifier.
   * @param arguments The modifier arguments.
   * @param alias Specifies whether the base of the call may be an alias of the symbol the call is
   *          associated with. A false value for this parameter indicates that the symbol and the
   *          base MUST point to each other.
   * @return An edge function, which may either be a {@link PropagationTransformer}, or a
   *         {@link TopPropagationTransformer}.
   */
  public static EdgeFunction<BasePropagationValue> makeTransformer(Unit callSite,
      Argument[] arguments, boolean alias) {
    PropagationTransformer result = new PropagationTransformer();

    if (callSite == null && arguments == null) {
      result.addPathTransformer(NullPathTransformer.v());
    } else {
      result.addPathTransformer(IdentityPathTransformer.v());

      for (Argument argument : arguments) {
        PropagationTransformer newTransformer = makeTransformerForArgument(callSite, argument);
        EdgeFunction<BasePropagationValue> newResult = result.composeWith(newTransformer);
        if (newResult instanceof TopPropagationTransformer) {
          return TopPropagationTransformer.v();
        } else {
          result = (PropagationTransformer) newResult;
        }
      }
    }

    return result.intern();
  }

  /**
   * Returns a {@link PropagationTransformer} for a given modifier and a given argument.
   * 
   * @param callSite A call site that is a COAL modifier.
   * @param argument An {@link edu.psu.cse.siis.coal.arguments.Argument Argument}.
   * @return A PropagationTransformer.
   */
  private static PropagationTransformer
      makeTransformerForArgument(Unit callSite, Argument argument) {
    PropagationTransformer transformer = new PropagationTransformer();

    if (Model.v().isModeledType(argument.getType())) {
      // The argument is a modeled type. We cannot necessarily directly infer the influence of the
      // statement.
      String[] actions = argument.getActions();

      if (AnalysisParameters.v().isIterative()) {
        String field = argument.getFieldName();
        Set<FieldTransformer> fieldTransformers =
            IterationSolver.v().makeTransformersFromReferencedValue(callSite,
                ((Stmt) callSite).getInvokeExpr().getArg(argument.getArgnum()[0]), field,
                argument.getNominalFieldType(), actions[0]);
        for (FieldTransformer fieldTransformer : fieldTransformers) {
          PathTransformer pathTransformer = new PathTransformer();
          pathTransformer.addFieldTransformer(field, fieldTransformer);
          transformer.addPathTransformer(pathTransformer.intern());
        }
      } else {
        FieldTransformer fieldTransformer =
            FieldTransformerManager.v().makeFieldTransformer(Constants.DefaultActions.COMPOSE,
                ((Stmt) callSite).getInvokeExpr().getArg(argument.getArgnum()[0]), (Stmt) callSite,
                actions[0]);

        PathTransformer pathTransformer = new PathTransformer();
        pathTransformer.addFieldTransformer(argument.getFieldName(), fieldTransformer);
        transformer.addPathTransformer(pathTransformer.intern());
      }
    } else {
      Set<Object> values = ArgumentValueManager.v().getArgumentValues(argument, callSite);

      makePlainTransformer(values, transformer, argument, callSite);
    }

    return transformer.intern();
  }

  /**
   * Populates a {@link PropagationTransformer} for a given COAL modifier. This method should only
   * be called for argument values that are not types modeled using COAL.
   * 
   * @param values The possible values for the modifier argument.
   * @param transformer The propagation transformer to populate.
   * @param argument The COAL modifier {@link edu.psu.cse.siis.coal.arguments.Argument Argument}.
   * @param callSite A call site that is a COAL modifier.
   */
  private static void makePlainTransformer(Set<Object> values, PropagationTransformer transformer,
      Argument argument, Unit callSite) {
    if (values == null) {
      addPathTransformer(transformer, argument, null);
    } else {
      if (values.size() == 0) {
        // Example: com.tresksoft.toolbox.
        logger.error(callSite.toString());
        logger.error(AnalysisParameters.v().getIcfg().getMethodOf(callSite).toString());
        logger.error("Warning: Missing argument value for type " + argument.getType()
            + ". Setting to (.*)");
        values.add("(.*)");
      }
      for (Object value : values) {
        addPathTransformer(transformer, argument, value);
      }
    }
  }

  /**
   * Adds a {@link PathTransformer} to a {@link PropagationTransformer}, given an argument and an
   * argument value.
   * 
   * @param transformer A propagation transformer.
   * @param argument A COAL modifier {@link edu.psu.cse.siis.coal.arguments.Argument Argument}.
   * @param value A possible value for the modifier argument.
   */
  private static void addPathTransformer(PropagationTransformer transformer, Argument argument,
      Object value) {
    FieldTransformer fieldTransformer = IdentityFieldTransformer.v();

    if (value instanceof SourceDescriptor) {
      if (AnalysisParameters.v().isIterative()) {
        SourceDescriptor sourceDescriptor = (SourceDescriptor) value;
        String[] actions = argument.getActions();
        if (actions.length != 1) {
          throw new RuntimeException("Not implemented with more than one action at stmt");
        }
        String field = argument.getFieldName();
        Set<FieldTransformer> fieldTransformers =
            IterationSolver.v().makeTransformersFromReferencedValue(sourceDescriptor.getStmt(),
                sourceDescriptor.getSymbol(), field, argument.getNominalFieldType(), actions[0]);

        for (FieldTransformer fieldTransformer2 : fieldTransformers) {
          PathTransformer pathTransformer = new PathTransformer();
          pathTransformer.addFieldTransformer(field, fieldTransformer2);
          transformer.addPathTransformer(pathTransformer.intern());
        }
        return;
      } else {
        for (String action : argument.getActions()) {
          SourceDescriptor sourceDescriptor = (SourceDescriptor) value;
          fieldTransformer =
              fieldTransformer.compose(FieldTransformerManager.v().makeFieldTransformer(
                  Constants.DefaultActions.COMPOSE, sourceDescriptor.getSymbol(),
                  sourceDescriptor.getStmt(), action));
        }
      }
    } else {
      for (String action : argument.getActions()) {
        fieldTransformer =
            fieldTransformer.compose(FieldTransformerManager.v()
                .makeFieldTransformer(action, value));
      }

    }

    PathTransformer pathTransformer = new PathTransformer();
    pathTransformer.addFieldTransformer(argument.getFieldName(), fieldTransformer);
    transformer.addPathTransformer(pathTransformer.intern());
  }
}
