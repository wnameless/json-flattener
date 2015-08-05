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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * 
 * {@link JsonUnflattener} provides a static {@link #unflatten(String)} method
 * to unflatten any flattened JSON string back to nested one.
 *
 */
public final class JsonUnflattener {

  private JsonUnflattener() {}

  private static final Pattern keyPartPattern =
      Pattern.compile("\\[\\s*\\d+\\s*\\]|\\[\\s*\".*\"\\s*\\]|[^\\.\\[\\]]+");

  /**
   * Returns a JSON string of nested objects by the given flattened JSON string.
   * 
   * @param json
   *          a flattened JSON string
   * @return a JSON string of nested objects
   */
  public static String unflatten(String json) {
    JsonObject flattened = Json.parse(json).asObject();
    JsonValue unflattened = null;

    for (String key : flattened.names()) {
      JsonValue currentVal = unflattened;
      String objKey = null;
      Integer aryIdx = null;

      Matcher matcher = keyPartPattern.matcher(key);
      while (matcher.find()) {
        String keyPart = matcher.group();

        if (objKey != null ^ aryIdx != null) {
          if (matchJsonArray(keyPart)) {
            currentVal = findOrCreateJsonArray(currentVal, objKey, aryIdx);
            objKey = null;
            aryIdx = extractIndex(keyPart);
          } else {
            currentVal = findOrCreateJsonObject(currentVal, objKey, aryIdx);
            objKey = extractKey(keyPart);
            aryIdx = null;
          }
        }

        if (objKey == null && aryIdx == null) {
          if (matchJsonArray(keyPart)) {
            aryIdx = extractIndex(keyPart);
            if (currentVal == null) currentVal = Json.array();
          } else {
            objKey = keyPart.replace(".", "");
            if (currentVal == null) currentVal = Json.object();
          }
        }

        if (unflattened == null) unflattened = currentVal;
      }

      setUnflattenedValue(flattened, key, currentVal, objKey, aryIdx);
    }

    return unflattened.toString();
  }

  private static String extractKey(String keyPart) {
    if (keyPart.matches("^\\[\\s*\".*$"))
      return keyPart.replaceAll("^\\[\\s*\"", "").replaceAll("\"\\s*\\]$", "");
    else
      return keyPart;
  }

  private static Integer extractIndex(String keyPart) {
    return Integer.valueOf(keyPart.replaceAll("[\\[\\]\\s]", ""));
  }

  private static boolean matchJsonArray(String keyPart) {
    return keyPart.matches("\\[\\s*\\d+\\s*\\]");
  }

  private static JsonValue findOrCreateJsonArray(JsonValue currentVal,
      String objKey, Integer aryIdx) {
    if (objKey != null) {
      if (currentVal.asObject().get(objKey) == null) {
        JsonValue ary = Json.array();
        currentVal.asObject().add(objKey, ary);

        return ary;
      }

      return currentVal.asObject().get(objKey);
    } else {
      if (currentVal.asArray().size() <= aryIdx
          || currentVal.asArray().get(aryIdx) == null) {
        JsonValue ary = Json.array();
        assureJsonArraySize(currentVal.asArray(), aryIdx);
        currentVal.asArray().set(aryIdx, ary);

        return ary;
      }

      return currentVal.asArray().get(aryIdx);
    }
  }

  private static JsonValue findOrCreateJsonObject(JsonValue currentVal,
      String objKey, Integer aryIdx) {
    if (objKey != null) {
      if (currentVal.asObject().get(objKey) == null) {
        JsonValue obj = Json.object();
        currentVal.asObject().add(objKey, obj);

        return obj;
      }
      return currentVal.asObject().get(objKey);
    } else {
      if (currentVal.asArray().size() <= aryIdx
          || currentVal.asArray().get(aryIdx) == null) {
        JsonValue obj = Json.object();
        assureJsonArraySize(currentVal.asArray(), aryIdx);
        currentVal.asArray().set(aryIdx, obj);

        return obj;
      }

      return currentVal.asArray().get(aryIdx);
    }
  }

  private static void setUnflattenedValue(JsonObject flattened, String key,
      JsonValue currentVal, String objKey, Integer aryIdx) {
    if (objKey != null) {
      currentVal.asObject().add(objKey, flattened.get(key));
    } else if (aryIdx != null) {
      assureJsonArraySize(currentVal.asArray(), aryIdx);
      currentVal.asArray().set(aryIdx, flattened.get(key));
    }
  }

  private static void assureJsonArraySize(JsonArray jsonArray, Integer index) {
    while (index >= jsonArray.size()) {
      jsonArray.add(Json.NULL);
    }
  }

}
