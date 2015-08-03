/**
 *
 * @author Wei-Ming Wu
 *
 *
 * Copyright 2015 Wei-Ming Wu
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
package com.github.wnameless.json.unflattener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

/**
 * 
 * {@link JsonUnflattener} provides a static {@link #unflatten(String)} method
 * to unflatten any flattened JSON string back to nested one.
 *
 */
public final class JsonUnflattener {

  private static final Pattern pattern =
      Pattern.compile("\\[\\d+\\]|[^\\.\\[\\]]+");

  /**
   * Returns a a nested JSON string by the given flattened JSON string.
   * 
   * @param json
   *          a flattened JSON string
   * @return a nested JSON string
   */
  public static String unflatten(String json) {
    JsonValue flatten = Json.parse(json);
    if (!flatten.isObject())
      throw new IllegalArgumentException();

    JsonValue unflatten = null;

    List<String> names = new ArrayList<String>(flatten.asObject().names());
    while (!names.isEmpty()) {
      JsonValue currentVal = unflatten;
      String objKey = null;
      Integer aryIdx = null;

      String key = names.remove(0);
      Matcher matcher = pattern.matcher(key);
      while (matcher.find()) {
        String keyPart = matcher.group();

        if (objKey != null) {
          if (keyPart.startsWith("[")) {
            if (currentVal.asObject().get(objKey) == null) {
              JsonValue ary = Json.array();
              currentVal.asObject().add(objKey, ary);
              currentVal = ary;
            } else {
              currentVal = currentVal.asObject().get(objKey);
            }
            aryIdx = Integer.valueOf(keyPart.replace("[", "").replace("]", ""));
            objKey = null;
          } else {
            if (currentVal.asObject().get(objKey) == null) {
              JsonValue obj = Json.object();
              currentVal.asObject().add(objKey, obj);
              currentVal = obj;
            } else {
              currentVal = currentVal.asObject().get(objKey);
            }
            objKey = keyPart.replace(".", "");
          }
        } else if (aryIdx != null) {
          if (keyPart.startsWith("[")) {
            if (currentVal.asArray().get(aryIdx) == null) {
              JsonValue ary = Json.array();
              increaseJsonArraySize(currentVal.asArray(), aryIdx);
              currentVal.asArray().set(aryIdx, ary);
              currentVal = ary;
            } else {
              currentVal = currentVal.asArray().get(aryIdx);
            }
            aryIdx = Integer.valueOf(keyPart.replace("[", "").replace("]", ""));
          } else {
            if (currentVal.asArray().get(aryIdx) == null) {
              JsonValue obj = Json.object();
              increaseJsonArraySize(currentVal.asArray(), aryIdx);
              currentVal.asArray().set(aryIdx, obj);
              currentVal = obj;
            } else {
              currentVal = currentVal.asArray().get(aryIdx);
            }
            objKey = keyPart.replace(".", "");
            aryIdx = null;
          }
        } else {
          if (keyPart.startsWith("[")) {
            aryIdx = Integer.valueOf(keyPart.replace("[", "").replace("]", ""));
            if (currentVal == null)
              currentVal = Json.array();
          } else {
            objKey = keyPart.replace(".", "");
            if (currentVal == null)
              currentVal = Json.object();
          }
        }

        if (unflatten == null)
          unflatten = currentVal;
      }

      if (objKey != null) {
        currentVal.asObject().add(objKey, flatten.asObject().get(key));
      } else if (aryIdx != null) {
        increaseJsonArraySize(currentVal.asArray(), aryIdx);
        currentVal.asArray().set(aryIdx, flatten.asObject().get(key));
      }
    }

    return unflatten.toString();
  }

  private static void increaseJsonArraySize(JsonArray jsonArray,
      Integer index) {
    while (index >= jsonArray.size()) {
      jsonArray.add(Json.NULL);
    }
  }

  public static void main(String... args) {
    System.out.println(unflatten(
        "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"));
  }

}
