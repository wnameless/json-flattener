/*
 *
 * Copyright 2016 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.json.flattener;

/**
 * 
 * {@link PrintMode} lists all acceptable JSON print mode of the
 * {@link JsonFlattener}.
 * 
 * @author Wei-Ming Wu
 *
 */
public enum PrintMode {

  /**
   * Prints output as minified JSON.
   */
  MINIMAL,

  /**
   * Prints each objects in different lines with necessary indentations.
   */
  PRETTY;

  /**
   * Returns a pretty print JSON of any JSON input.
   * 
   * @param json
   *          any JSON
   * @return a pretty print JSON
   */
  public static String prettyPrint(String json) {
    StringBuilder prettyPrintBuilder = new StringBuilder();

    int indentLevel = 0;
    boolean inQuote = false;
    boolean inBracket = false;
    for (char jsonChar : json.toCharArray()) {
      switch (jsonChar) {
        case '"':
          inQuote = !inQuote;
          prettyPrintBuilder.append(jsonChar);
          break;
        case ' ':
        case '\t':
          if (inQuote) {
            prettyPrintBuilder.append(jsonChar);
          }
          break;
        case '{':
          prettyPrintBuilder.append(jsonChar);
          if (!inQuote) {
            indentLevel++;
            appendNewLine(indentLevel, prettyPrintBuilder);
          }
          break;
        case '}':
          if (!inQuote) {
            indentLevel--;
            appendNewLine(indentLevel, prettyPrintBuilder);
          }
          prettyPrintBuilder.append(jsonChar);
          break;
        case '[':
          prettyPrintBuilder.append(jsonChar);
          if (!inQuote) {
            inBracket = true;
            prettyPrintBuilder.append(' ');
          }
          break;
        case ']':
          if (!inQuote) {
            inBracket = false;
            prettyPrintBuilder.append(' ');
          }
          prettyPrintBuilder.append(jsonChar);
          break;
        case ',':
          prettyPrintBuilder.append(jsonChar);
          if (!inQuote) {
            if (inBracket) {
              prettyPrintBuilder.append(' ');
            } else {
              appendNewLine(indentLevel, prettyPrintBuilder);
            }
          }
          break;
        case ':':
          if (inQuote) {
            prettyPrintBuilder.append(jsonChar);
          } else {
            prettyPrintBuilder.append(' ');
            prettyPrintBuilder.append(jsonChar);
            prettyPrintBuilder.append(' ');
          }
          break;
        default:
          prettyPrintBuilder.append(jsonChar);
      }
    }

    return prettyPrintBuilder.toString();
  }

  private static void appendNewLine(int indentLevel,
      StringBuilder stringBuilder) {
    stringBuilder.append('\n');
    for (int i = 0; i < indentLevel; i++) {
      stringBuilder.append(' ');
      stringBuilder.append(' ');
    }
  }

}
