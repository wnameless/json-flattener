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

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
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
   * Returns a JSON Unflattener without doing any preprocessing on the input
   * JSON string. It creates a JSON Unflattener instance way more faster than
   * the normal constructor because it performs a LAZY initialization
   * mechanism.<br>
   * <br>
   * WARN: Due to the LAZY initialization, the malformed input of JSON string
   * cannot be detected until any unflattening has been executed.
   * 
   * @param json
   *          the JSON string
   * @return a JSON unflattener
   */
  public static JsonUnflattener lazy(String json) {
    return new JsonUnflattener(json, true);
  }

  /**
   * Returns a JSON Unflattener without doing any preprocessing on the input
   * JSON reader. It creates a JSON Unflattener instance way more faster than
   * the normal constructor because it performs a LAZY initialization
   * mechanism.<br>
   * <br>
   * WARN: Due to the LAZY initialization, the malformed input of JSON reader
   * cannot be detected until any unflattening has been executed.
   * 
   * @param jsonReader
   *          the JSON reader
   * @return a JSON unflattener
   */
  public static JsonUnflattener lazy(Reader jsonReader) {
    return new JsonUnflattener(jsonReader, true);
  }

  private String rawJson;
  private Reader jsonReader;
  private JsonValue root;

  private Character separator = '.';
  private Character leftBracket = '[';
  private Character rightBracket = ']';
  private PrintMode printMode = PrintMode.MINIMAL;

  private JsonValue getRoot() {
    if (root == null) {
      if (rawJson != null) {
        root = Json.parse(rawJson);
      } else {
        try {
          root = Json.parse(jsonReader);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return root;
  }

  private JsonUnflattener(String json, boolean isLazy) {
    rawJson = notNull(json);
    if (!isLazy) getRoot();
  }

  private JsonUnflattener(Reader jsonReader, boolean isLazy) {
    this.jsonReader = notNull(jsonReader);
    if (!isLazy) getRoot();
  }

  /**
   * Creates a JSON unflattener.
   * 
   * @param json
   *          the JSON string
   */
  public JsonUnflattener(String json) {
    rawJson = notNull(json);
    getRoot();
  }

  /**
   * Creates a JSON unflattener.
   * 
   * @param jsonReader
   *          the JSON reader
   */
  public JsonUnflattener(Reader jsonReader) {
    this.jsonReader = notNull(jsonReader);
    getRoot();
  }

  private String arrayIndex() {
    return Pattern.quote(leftBracket.toString()) + "\\s*\\d+\\s*"
        + Pattern.quote(rightBracket.toString());
  }

  private String objectComplexKey() {
    return Pattern.quote(leftBracket.toString()) + "\\s*\".+?\"\\s*"
        + Pattern.quote(rightBracket.toString());
  }

  private String objectKey() {
    return "[^" + Pattern.quote(separator.toString())
        + Pattern.quote(leftBracket.toString())
        + Pattern.quote(rightBracket.toString()) + "\\s]+";
  }

  private Pattern keyPartPattern() {
    return Pattern
        .compile(arrayIndex() + "|" + objectComplexKey() + "|" + objectKey());
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
    isTrue(!Character.toString(separator).matches("[\"\\s]"),
        "Separator contains illegal chracter(%s)",
        Character.toString(separator));
    isTrue(!leftBracket.equals(separator) && !rightBracket.equals(separator),
        "Separator(%s) is already used in brackets",
        Character.toString(separator));

    this.separator = separator;
    return this;
  }

  private String illegalBracketsRegex() {
    return "[\"\\s" + Pattern.quote(separator.toString()) + "]";
  }

  /**
   * A fluent setter to setup the left and right brackets within a key in the
   * flattened JSON. The default left and right brackets are left square
   * bracket([) and right square bracket(]).
   * 
   * @param leftBracket
   *          any character
   * @param rightBracket
   *          any character
   * @return this {@link JsonUnflattener}
   */
  public JsonUnflattener withLeftAndRightBrackets(char leftBracket,
      char rightBracket) {
    isTrue(leftBracket != rightBracket, "Both brackets cannot be the same");
    isTrue(!Character.toString(leftBracket).matches(illegalBracketsRegex()),
        "Left bracket contains illegal chracter(%s)",
        Character.toString(leftBracket));
    isTrue(!Character.toString(rightBracket).matches(illegalBracketsRegex()),
        "Right bracket contains illegal chracter(%s)",
        Character.toString(rightBracket));

    this.leftBracket = leftBracket;
    this.rightBracket = rightBracket;
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
    this.printMode = notNull(printMode);
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
   * @return a JSON string of nested objects
   */
  public String unflatten() {
    StringWriter sw = new StringWriter();
    if (getRoot().isArray()) {
      try {
        unflattenArray(getRoot().asArray()).writeTo(sw, getWriterConfig());
      } catch (IOException e) {}
      return sw.toString();
    }
    if (!getRoot().isObject()) {
      return getRoot().toString();
    }

    JsonObject flattened = getRoot().asObject();
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
    if (keyPart.matches(objectComplexKey()))
      return keyPart
          .replaceAll("^" + Pattern.quote(leftBracket.toString()) + "\\s*\"",
              "")
          .replaceAll("\"\\s*" + Pattern.quote(rightBracket.toString()) + "$",
              "");
    else
      return keyPart;
  }

  private Integer extractIndex(String keyPart) {
    return Integer
        .valueOf(keyPart.replaceAll("[" + Pattern.quote(leftBracket.toString())
            + Pattern.quote(rightBracket.toString()) + "\\s]", ""));
  }

  private boolean isJsonArray(String keyPart) {
    return keyPart.matches(arrayIndex());
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

  @Override
  public int hashCode() {
    int result = 27;
    result = 31 * result + getRoot().hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JsonUnflattener)) return false;
    return getRoot().equals(((JsonUnflattener) o).getRoot());
  }

  @Override
  public String toString() {
    return "JsonUnflattener{root=" + getRoot() + "}";
  }

}
