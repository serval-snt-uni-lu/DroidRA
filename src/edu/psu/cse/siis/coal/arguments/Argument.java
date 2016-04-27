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
package edu.psu.cse.siis.coal.arguments;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An argument for a modifier or a hotspot.
 */
public class Argument implements Serializable {
  private static final long serialVersionUID = 1L;

  private int[] argnum;
  private String type;
  private String[] actions;
  private String[] inlineValues;
  private Map<String, String> properties;
  private short count = -1;
  private Field field;
  private String referencedFieldName;

  public Argument() {
  }

  public Argument(Argument other) {
    this.argnum = other.argnum;
    this.type = other.type;
    this.field = other.field;
    this.actions = other.actions;
    this.inlineValues = other.inlineValues;
    if (other.properties != null) {
      this.properties = new HashMap<>(other.properties);
    }
    this.count = other.count;
  }

  /**
   * Sets the argument number. It is an array of integers because a modifier argument may be given
   * by several actual method arguments. The way that arguments are determined is specified by the
   * argument analysis that is registered for this argument type.
   * 
   * @param argnum The argument number.
   */
  public void setArgnum(int[] argnum) {
    this.argnum = argnum;
  }

  /**
   * Sets the type of the argument. The specified type should be registered using
   * {@link ArgumentValueManager#registerArgumentValueAnalysis}.
   * 
   * @param type The argument type.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Sets the field to which the argument refers.
   * 
   * @param field The field to set.
   */
  public void setField(Field field) {
    this.field = field;
  }

  /**
   * Sets the actions to be performed with the argument. If more than one action is specified, all
   * action are composed in the order that they are specified. Any action specified here should be
   * registered using
   * {@link edu.psu.cse.siis.coal.field.transformers.FieldTransformerManager#registerFieldTransformerFactory
   * registerFieldTransformerFactory} .
   * 
   * @param actions The actions to be performed.
   */
  public void setActions(String[] actions) {
    this.actions = actions;
  }

  /**
   * Sets the inline values for the argument. Inline values are specified in COAL when the value of
   * an argument is always the same. If more than one value is specified, then they lead to several
   * different field transformers.
   * 
   * @param inlineValues The inline values to set.
   */
  public void setInlineValues(String[] inlineValues) {
    this.inlineValues = inlineValues;
  }

  /**
   * Adds a property for this argument. Properties are key-value pairs that can be specified in the
   * COAL specification and retrieve later, for example when processing the results of the analysis.
   * 
   * @param key A key.
   * @param value A value associated with the argument key.
   */
  public void addProperty(String key, String value) {
    if (this.properties == null) {
      this.properties = new HashMap<>();
    }
    this.properties.put(key, value);
  }

  /**
   * Sets the number of values expected to be associated with a field. If this is set to 1, then the
   * argument cannot have more than one value, so do not set to 1 unless the field cannot contain
   * more than one value. For example, this can be useful when a field is a single string and not a
   * set of strings.
   * 
   * @param count The expected count.
   */
  public void setCount(short count) {
    this.count = count;
  }

  /**
   * Sets the name of a referenced field. This is useful when an argument is a value modeled using
   * COAL. This specifies which field from the modeled value we are interested in.
   * 
   * @param referencedFieldName The referenced field name.
   */
  public void setReferencedFieldName(String referencedFieldName) {
    this.referencedFieldName = referencedFieldName;
  }

  /**
   * Returns the argument number for this argument. It is an array of integers because a modifier
   * argument may be given by several actual method arguments. The way that arguments are determined
   * is specified by the argument analysis that is registered for this argument type.
   * 
   * @return The argument number.
   */
  public int[] getArgnum() {
    return argnum;
  }

  /**
   * Returns the type of the field influenced by this argument.
   * 
   * @return The field type.
   */
  public String getType() {
    if (type == null) {
      return field.getType();
    }

    return type;
  }

  /**
   * Returns the nominal field type for the field modified by this argument. This is the type
   * declared for the field. The argument value analysis used to determine the argument is the one
   * returned by {@link #getType}.
   * 
   * @return The nominal field type.
   */
  public String getNominalFieldType() {
    return field.getType();
  }

  /**
   * Returns the name of the field modified by this argument.
   * 
   * @return The field name.
   */
  public String getFieldName() {
    return field.getName();
  }

  /**
   * Returns the operations performed by this argument on the field.
   * 
   * @return The operations performed by this argument on the field.
   */
  public String[] getActions() {
    return actions;
  }

  /**
   * Returns the inline values for the argument. Inline values are specified in COAL when the value
   * of an argument is always the same. If more than one value is specified, then they lead to
   * several different field transformers.
   * 
   * @return The inline values for the argument.
   */
  public String[] getInlineValues() {
    return inlineValues;
  }

  /**
   * Returns the property for a given key.
   * 
   * @param key A key.
   * @return The property for the key.
   */
  public String getProperty(String key) {
    if (this.properties == null) {
      return null;
    }
    return this.properties.get(key);
  }

  /**
   * Returns the map of properties for this argument. Properties are key-value pairs that can be
   * specified in the COAL specification and retrieve later, for example when processing the results
   * of the analysis.
   * 
   * @return The properties for this argument.
   */
  public Map<String, String> getProperties() {
    return this.properties;
  }

  /**
   * Returns the referenced field name for an argument that is a COAL value. This specifies which
   * field from the modeled value we are interested in.
   * 
   * @return The referenced field name.
   */
  public String getReferencedFieldName() {
    return this.referencedFieldName;
  }

  /**
   * Returns the number of expected values for the field modified by this argument.
   * 
   * @return The expected count.
   */
  public short getCount() {
    return this.count;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    if (getArgnum() != null) {
      result.append("(");
      for (int argnum : getArgnum()) {
        if (first) {
          first = false;
        } else {
          result.append(",");
        }
        result.append(argnum);
      }
      result.append(") ");
    }
    result.append(getType());
    result.append(" ");
    result.append(getFieldName());
    result.append(" ");
    if (getActions() != null) {
      result.append("(");
      first = true;
      for (String action : getActions()) {
        if (first) {
          first = false;
        } else {
          result.append(",");
        }
        result.append(action);
      }
      result.append(")");
    }
    result.append(" ");
    if (getInlineValues() != null) {
      result.append("(");
      first = true;
      for (String inlineValue : getInlineValues()) {
        if (first) {
          first = false;
        } else {
          result.append(",");
        }
        result.append(inlineValue);
      }
      result.append(")");
    }
    return result.toString();
  }
}
