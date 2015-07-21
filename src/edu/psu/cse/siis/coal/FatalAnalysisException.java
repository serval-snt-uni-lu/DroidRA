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

/**
 * A fatal analysis exception. This should cause the analysis to stop but it can be caught at a
 * higher level to allow some processing before terminating (closing files, etc.).
 */
public class FatalAnalysisException extends AnalysisException {
  private static final long serialVersionUID = -4890708762379522641L;

  public FatalAnalysisException() {
    super();
  }

  public FatalAnalysisException(String message) {
    super(message);
  }

  public FatalAnalysisException(String message, Throwable cause) {
    super(message, cause);
  }

  public FatalAnalysisException(Throwable cause) {
    super(cause);
  }
}
