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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.cse.siis.coal.Constants;
import edu.psu.cse.siis.coal.Internable;
import edu.psu.cse.siis.coal.Pool;
import edu.psu.cse.siis.coal.values.BasePropagationValue;
import edu.psu.cse.siis.coal.values.PathValue;
import edu.psu.cse.siis.coal.values.PropagationValue;
import edu.psu.cse.siis.coal.values.TopPropagationValue;

/**
 * An MVMF constant propagation transformer. This is a set of {@link PathTransformer} elements and
 * therefore it accounts for several execution paths.
 */
public class PropagationTransformer implements EdgeFunction<BasePropagationValue>,
    Internable<PropagationTransformer> {
  private static final Pool<PropagationTransformer> POOL = new Pool<>();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Set<PathTransformer> pathTransformers = new HashSet<>(2);

  /**
   * Adds a {@link PathTransformer} to this propagation transformer.
   * 
   * @param pathTransformer A PathTransformer.
   */
  public void addPathTransformer(PathTransformer pathTransformer) {
    this.pathTransformers.add(pathTransformer);
  }

  @Override
  public PropagationValue computeTarget(BasePropagationValue source) {
    PropagationValue result = new PropagationValue();

    if (source instanceof PropagationValue) {
      for (PathTransformer pathTransformer : this.pathTransformers) {
        for (PathValue pathValue : ((PropagationValue) source).getPathValues()) {
          result.addPathValue(pathTransformer.computeTarget(pathValue));
        }
      }
    } else if (source instanceof TopPropagationValue) {
      for (PathTransformer pathTransformer : this.pathTransformers) {
        result.addPathValue(pathTransformer.computeTarget(new PathValue()));
      }
    }

    return result.intern();
  }

  @Override
  public EdgeFunction<BasePropagationValue> composeWith(
      EdgeFunction<BasePropagationValue> secondFunction) {
    if (logger.isDebugEnabled()) {
      logger.debug("Composing " + this + " with " + secondFunction);
    }
    if (secondFunction instanceof PropagationTransformer) {
      Set<PathTransformer> secondPathTransformers =
          ((PropagationTransformer) secondFunction).pathTransformers;
      PropagationTransformer result = new PropagationTransformer();

      for (PathTransformer pathTransformer : pathTransformers) {
        for (PathTransformer secondPathTransformer : secondPathTransformers) {
          result.pathTransformers.add(pathTransformer.compose(secondPathTransformer));
          if (result.pathTransformers.size() > Constants.VALUE_LIMIT) {
            return TopPropagationTransformer.v();
          }
        }
      }

      return result.intern();
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Returning " + this);
    }
    return this;
  }

  @Override
  public EdgeFunction<BasePropagationValue> joinWith(
      EdgeFunction<BasePropagationValue> otherFunction) {
    if (otherFunction instanceof PropagationTransformer) {
      PropagationTransformer result = new PropagationTransformer();
      result.pathTransformers.addAll(this.pathTransformers);
      result.pathTransformers.addAll(((PropagationTransformer) otherFunction).pathTransformers);

      if (result.pathTransformers.size() > Constants.VALUE_LIMIT) {
        return TopPropagationTransformer.v();
      }
      return result.intern();
    }
    return this;
  }

  @Override
  public boolean equalTo(EdgeFunction<BasePropagationValue> other) {
    if (!(other instanceof PropagationTransformer)) {
      return false;
    }
    PropagationTransformer secondTransformer = (PropagationTransformer) other;
    return this.pathTransformers.equals(secondTransformer.pathTransformers);
  }

  @Override
  public String toString() {
    StringBuilder result =
        new StringBuilder("Transformer: " + pathTransformers.size() + " values\n");
    for (PathTransformer pathTransformer : pathTransformers) {
      result.append(pathTransformer.toString());
      result.append("\n");
    }

    return result.toString();
  }

  @Override
  public int hashCode() {
    return this.pathTransformers.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof PropagationTransformer
        && Objects.equals(this.pathTransformers,
            ((PropagationTransformer) other).equals(pathTransformers));
  }

  @Override
  public PropagationTransformer intern() {
    return POOL.intern(this);
  }
}
