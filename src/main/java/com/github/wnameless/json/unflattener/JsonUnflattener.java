/*
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.PrettyPrint;
import com.eclipsesource.json.WriterConfig;
import com.github.wnameless.json.flattener.PrintMode;

/**
 * 
 * {@link JsonUnflattener} provides a static {@link #unflatten(String)} method
 * to unflatten any flattened JSON string back to nested one.
 *
 * @author Wei-Ming Wu
 * 
 */
public final class JsonUnflattener {

  private static final String arrayIndex = "\\[\\s*\\d+\\s*\\]";
  private static final String objectComplexKey = "\\[\\s*\".*\"\\s*\\]";

  private final JsonValue root;

  private Character separator = '.';
  private PrintMode printMode = PrintMode.MINIMAL;

  /**
   * Creates a JSON unflattener.
   * 
   * @param json
   *          the JSON string
   */
  public JsonUnflattener(String json) {
    root = Json.parse(json);
  }

  private String objectKey() {
    return "[^" + Pattern.quote(separator.toString()) + "\\[\\]]+";
  }

  private Pattern keyPartPattern() {
    return Pattern.compile(arrayIndex + "|" + objectComplexKey + "|"
        + objectKey());
  }

  /**
   * A fluent setter to setup the separator within a key in the flattened JSON.
   * The default separator is a dot(.).
   * 
   * @param separator
   *          any character
   * @return this {@link JsonUnflattener}
   */
  public JsonUnflattener withSeparator(char separator) {
    this.separator = separator;
    return this;
  }

  /**
   * A fluent setter to setup a print mode of the {@link JsonUnflattener}. The
   * default print mode is minimal.
   * 
   * @param printMode
   *          a {@link PrintMode}
   * @return this {@link JsonUnflattener}
   */
  public JsonUnflattener withPrintMode(PrintMode printMode) {
    this.printMode = printMode;
    return this;
  }

  private WriterConfig getWriterConfig() {
    switch (printMode) {
      case REGULAR:
        return PrettyPrint.singleLine();
      case PRETTY:
        return WriterConfig.PRETTY_PRINT;
      default:
        return WriterConfig.MINIMAL;
    }
  }

  /**
   * Returns a JSON string of nested objects by the given flattened JSON string.
   * 
   * @param json
   *          a flattened JSON string
   * @return a JSON string of nested objects
   */
  public static String unflatten(String json) {
    return new JsonUnflattener(json).unflatten();
  }

  /**
   * Returns a JSON string of nested objects by the given flattened JSON string.
   * 
   * @return a JSON string of nested objects
   */
  public String unflatten() {
    StringWriter sw = new StringWriter();
    if (root.isArray()) {
      try {
        unflattenArray(root.asArray()).writeTo(sw, getWriterConfig());
      } catch (IOException e) {}
      return sw.toString();
    }
    if (!root.isObject()) {
      return root.toString();
    }

    JsonObject flattened = root.asObject();
    JsonValue unflattened = flattened.names().isEmpty() ? Json.object() : null;

    for (String key : flattened.names()) {
      JsonValue currentVal = unflattened;
      String objKey = null;
      Integer aryIdx = null;

      Matcher matcher = keyPartPattern().matcher(key);
      while (matcher.find()) {
        String keyPart = matcher.group();

        if (objKey != null ^ aryIdx != null) {
          if (isJsonArray(keyPart)) {
            currentVal = findOrCreateJsonArray(currentVal, objKey, aryIdx);
            objKey = null;
            aryIdx = extractIndex(keyPart);
          } else { // JSON object
            if (flattened.get(key).isArray()) { // KEEP_ARRAYS mode
              flattened.set(key, unflattenArray(flattened.get(key).asArray()));
            }
            currentVal = findOrCreateJsonObject(currentVal, objKey, aryIdx);
            objKey = extractKey(keyPart);
            aryIdx = null;
          }
        }

        if (objKey == null && aryIdx == null) {
          if (isJsonArray(keyPart)) {
            aryIdx = extractIndex(keyPart);
            if (currentVal == null) currentVal = Json.array();
          } else { // JSON object
            objKey = extractKey(keyPart);
            if (currentVal == null) currentVal = Json.object();
          }
        }

        if (unflattened == null) unflattened = currentVal;
      }

      setUnflattenedValue(flattened, key, currentVal, objKey, aryIdx);
    }

    try {
      unflattened.writeTo(sw, getWriterConfig());
    } catch (IOException e) {}
    return sw.toString();
  }

  private JsonArray unflattenArray(JsonArray array) {
    JsonArray unflattenArray = Json.array().asArray();

    for (JsonValue value : array) {
      if (value.isArray()) {
        unflattenArray.add(unflattenArray(value.asArray()));
      } else if (value.isObject()) {
        unflattenArray.add(Json.parse(new JsonUnflattener(value.toString())
            .withSeparator(separator).unflatten()));
      } else {
        unflattenArray.add(value);
      }
    }

    return unflattenArray;
  }

  private String extractKey(String keyPart) {
    if (keyPart.matches(objectComplexKey))
      return keyPart.replaceAll("^\\[\\s*\"", "").replaceAll("\"\\s*\\]$", "");
    else
      return keyPart;
  }

  private static Integer extractIndex(String keyPart) {
    return Integer.valueOf(keyPart.replaceAll("[\\[\\]\\s]", ""));
  }

  private static boolean isJsonArray(String keyPart) {
    return keyPart.matches(arrayIndex);
  }

  private JsonValue findOrCreateJsonArray(JsonValue currentVal, String objKey,
      Integer aryIdx) {
    if (objKey != null) {
      JsonObject jsonObj = currentVal.asObject();

      if (jsonObj.get(objKey) == null) {
        JsonValue ary = Json.array();
        jsonObj.add(objKey, ary);

        return ary;
      }

      return jsonObj.get(objKey);
    } else { // aryIdx != null
      JsonArray jsonAry = currentVal.asArray();

      if (jsonAry.size() <= aryIdx || jsonAry.get(aryIdx).equals(Json.NULL)) {
        JsonValue ary = Json.array();
        assureJsonArraySize(jsonAry, aryIdx);
        jsonAry.set(aryIdx, ary);

        return ary;
      }

      return jsonAry.get(aryIdx);
    }
  }

  private JsonValue findOrCreateJsonObject(JsonValue currentVal, String objKey,
      Integer aryIdx) {
    if (objKey != null) {
      JsonObject jsonObj = currentVal.asObject();

      if (jsonObj.get(objKey) == null) {
        JsonValue obj = Json.object();
        jsonObj.add(objKey, obj);

        return obj;
      }

      return jsonObj.get(objKey);
    } else { // aryIdx != null
      JsonArray jsonAry = currentVal.asArray();

      if (jsonAry.size() <= aryIdx || jsonAry.get(aryIdx).equals(Json.NULL)) {
        JsonValue obj = Json.object();
        assureJsonArraySize(jsonAry, aryIdx);
        jsonAry.set(aryIdx, obj);

        return obj;
      }

      return jsonAry.get(aryIdx);
    }
  }

  private void setUnflattenedValue(JsonObject flattened, String key,
      JsonValue currentVal, String objKey, Integer aryIdx) {
    if (objKey != null) {
      currentVal.asObject().add(objKey, flattened.get(key));
    } else { // aryIdx != null
      assureJsonArraySize(currentVal.asArray(), aryIdx);
      currentVal.asArray().set(aryIdx, flattened.get(key));
    }
  }

  private void assureJsonArraySize(JsonArray jsonArray, Integer index) {
    while (index >= jsonArray.size()) {
      jsonArray.add(Json.NULL);
    }
  }

}
