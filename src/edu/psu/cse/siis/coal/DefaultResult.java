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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import soot.SootMethod;
import soot.Unit;

/**
 * A default analysis result. This extends {@link Result} with a simple method to dump the result to
 * stdout. The results are sorted alphabetically by (fully qualified) class name and method name.
 */
public class DefaultResult extends Result {
  public void dump() {
    System.out.println("*****Result*****");
    List<String> results = new ArrayList<>();

    for (Map.Entry<Unit, Map<Integer, Object>> entry : getResults().entrySet()) {
      Unit unit = entry.getKey();
      
      
      
      if ("r25 = virtualinvoke r2.<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>(r3)".equals(unit))
    	  System.out.println(unit); 
    	  
      SootMethod method = AnalysisParameters.v().getIcfg().getMethodOf(unit);
      String current =
          method.getDeclaringClass().getName() + "/" + method.getSubSignature() + " : " + unit
              + "\n";

      for (Map.Entry<Integer, Object> entry2 : entry.getValue().entrySet()) {
        current += "    " + entry2.getKey() + " : " + entry2.getValue() + "\n";
      }
      results.add(current);
    }
    Collections.sort(results);

    for (String result : results) {
      System.out.println(result);
    }
  }
}
