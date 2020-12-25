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

import static com.github.wnameless.json.flattener.FlattenMode.MONGODB;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.wnameless.json.base.JacksonJsonCore;
import com.github.wnameless.json.base.JsonArrayCore;
import com.github.wnameless.json.base.JsonCore;
import com.github.wnameless.json.base.JsonObjectCore;
import com.github.wnameless.json.base.JsonPrinter;
import com.github.wnameless.json.base.JsonValueBase;
import com.github.wnameless.json.base.JsonValueCore;
import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonifyLinkedHashMap;
import com.github.wnameless.json.flattener.KeyTransformer;
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
   * {@link ROOT} is the default key of the Map returned by
   * {@link #unflattenAsMap}. When {@link JsonUnflattener} processes a JSON
   * string which is not a JSON object or array, the final outcome may not suit
   * in a Java Map. At that moment, {@link JsonUnflattener} will put the result
   * in the Map with {@link ROOT} as its key.
   */
  public static final String ROOT = "root";

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
   * Returns a JSON string of nested objects by the given flattened Map.
   * 
   * @param flattenedMap
   *          a flattened Map
   * @return a JSON string of nested objects
   */
  public static String unflatten(Map<String, ?> flattenedMap) {
    return new JsonUnflattener(flattenedMap).unflatten();
  }

  /**
   * Returns a Java Map of nested objects by the given flattened JSON string.
   * 
   * @param json
   *          a flattened JSON string
   * @return a Java Map of nested objects
   */
  public static Map<String, Object> unflattenAsMap(String json) {
    return new JsonUnflattener(json).unflattenAsMap();
  }

  /**
   * Returns a Java Map of nested objects by the given flattened Map.
   * 
   * @param flattenedMap
   *          a flattened Map
   * @return a Java Map of nested objects
   */
  public static Map<String, Object> unflattenAsMap(
      Map<String, ?> flattenedMap) {
    return new JsonUnflattener(flattenedMap).unflattenAsMap();
  }

  private final JsonCore<?> jsonCore;
  private final JsonValueCore<?> root;

  private FlattenMode flattenMode = FlattenMode.NORMAL;
  private Character separator = '.';
  private Character leftBracket = '[';
  private Character rightBracket = ']';
  private PrintMode printMode = PrintMode.MINIMAL;
  private KeyTransformer keyTrans = null;

  private JsonUnflattener newJsonUnflattener(JsonValueCore<?> jsonValue) {
    JsonUnflattener ju = new JsonUnflattener(jsonValue);
    ju.withFlattenMode(flattenMode);
    ju.withSeparator(separator);
    ju.withLeftAndRightBrackets(leftBracket, rightBracket);
    ju.withPrintMode(printMode);
    if (keyTrans != null) ju.withKeyTransformer(keyTrans);
    return ju;
  }

  private JsonUnflattener(JsonValueCore<?> root) {
    jsonCore = new JacksonJsonCore();
    this.root = root;
  }

  private JsonValueCore<?> parseJson(String json) {
    return jsonCore.parse(json);
  }

  /**
   * Creates a JSON unflattener by given JSON string.
   * 
   * @param json
   *          a JSON string
   */
  public JsonUnflattener(String json) {
    jsonCore = new JacksonJsonCore();
    root = parseJson(json);
  }

  /**
   * Creates a JSON unflattener by given {@link JsonCore} and JSON string.
   * 
   * @param jsonCore
   *          a {@link JsonCore}
   * @param json
   *          a JSON string
   */
  public JsonUnflattener(JsonCore<?> jsonCore, String json) {
    this.jsonCore = notNull(jsonCore);
    root = parseJson(json);
  }

  /**
   * Creates a JSON unflattener by given JSON string reader.
   * 
   * @param jsonReader
   *          a JSON reader
   * @throws IOException
   *           if the jsonReader cannot be read
   */
  public JsonUnflattener(Reader jsonReader) throws IOException {
    jsonCore = new JacksonJsonCore();
    root = jsonCore.parse(jsonReader);
  }

  /**
   * Creates a JSON unflattener by given {@link JsonCore} and JSON string
   * reader.
   * 
   * @param jsonCore
   *          a {@link JsonCore}
   * @param jsonReader
   *          a JSON reader
   * @throws IOException
   *           if the jsonReader cannot be read
   */
  public JsonUnflattener(JsonCore<?> jsonCore, Reader jsonReader)
      throws IOException {
    this.jsonCore = notNull(jsonCore);
    root = jsonCore.parse(jsonReader);
  }

  /**
   * Creates a JSON unflattener by given flattened {@link Map}.
   * 
   * @param flattenedMap
   *          a flattened {@link Map}
   */
  public JsonUnflattener(Map<String, ?> flattenedMap) {
    jsonCore = new JacksonJsonCore();
    root = jsonCore.parse(new JsonifyLinkedHashMap<>(flattenedMap).toString());
  }

  /**
   * Creates a JSON unflattener by given {@link JsonCore} and flattened
   * {@link Map}.
   * 
   * @param jsonCore
   *          a {@link JsonCore}
   * @param flattenedMap
   *          a flattened {@link Map}
   */
  public JsonUnflattener(JsonCore<?> jsonCore, Map<String, ?> flattenedMap) {
    this.jsonCore = notNull(jsonCore);
    root = jsonCore.parse(new JsonifyLinkedHashMap<>(flattenedMap).toString());
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
        + Pattern.quote(rightBracket.toString()) + "]+";
  }

  private Pattern keyPartPattern() {
    if (flattenMode.equals(MONGODB))
      return Pattern.compile("[^" + Pattern.quote(separator.toString()) + "]+");
    else
      return Pattern
          .compile(arrayIndex() + "|" + objectComplexKey() + "|" + objectKey());
  }

  /**
   * A fluent setter to setup a mode of the {@link JsonUnflattener}.
   * 
   * @param flattenMode
   *          a {@link FlattenMode}
   * @return this {@link JsonUnflattener}
   */
  public JsonUnflattener withFlattenMode(FlattenMode flattenMode) {
    this.flattenMode = notNull(flattenMode);
    return this;
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
    String separatorStr = String.valueOf(separator);
    isTrue(!separatorStr.matches("[\"\\s]"),
        "Separator contains illegal character(%s)", separatorStr);
    isTrue(!leftBracket.equals(separator) && !rightBracket.equals(separator),
        "Separator(%s) is already used in brackets", separatorStr);

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
    String leftBracketStr = String.valueOf(leftBracket);
    String rightBracketStr = String.valueOf(rightBracket);
    isTrue(!leftBracketStr.matches(illegalBracketsRegex()),
        "Left bracket contains illegal character(%s)", leftBracketStr);
    isTrue(!rightBracketStr.matches(illegalBracketsRegex()),
        "Right bracket contains illegal character(%s)", rightBracketStr);

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

  /**
   * A fluent setter to setup a {@link KeyTransformer} of the
   * {@link JsonUnflattener}.
   * 
   * @param keyTrans
   *          a {@link KeyTransformer}
   * @return this {@link JsonUnflattener}
   */
  public JsonUnflattener withKeyTransformer(KeyTransformer keyTrans) {
    this.keyTrans = notNull(keyTrans);
    return this;
  }

  private String writeByConfig(JsonValueBase<?> jsonValue) {
    switch (printMode) {
      case PRETTY:
        return JsonPrinter.prettyPrint(jsonValue.toJson());
      default:
        return jsonValue.toJson();
    }
  }

  /**
   * Returns a JSON string of nested objects by the given flattened JSON string.
   * 
   * @return a JSON string of nested objects
   */
  public String unflatten() {
    StringWriter sw = new StringWriter();
    if (root.isArray()) {
      JsonArrayCore<?> unflattenedArray = unflattenArray(root.asArray());
      sw.append(writeByConfig(unflattenedArray.asValue()));
      return sw.toString();
    }
    if (!root.isObject()) {
      return root.toString();
    }

    JsonObjectCore<?> flattened = root.asObject();
    JsonValueCore<?> unflattened =
        flattened.isEmpty() ? jsonCore.parse("{}").asValue() : null;

    Iterator<String> names = flattened.names();
    while (names.hasNext()) {
      String key = names.next();
      JsonValueCore<?> currentVal = unflattened;
      String objKey = null;
      Integer aryIdx = null;

      Matcher matcher = keyPartPattern().matcher(key);
      while (matcher.find()) {
        String keyPart = matcher.group();

        if (objKey != null ^ aryIdx != null) {
          if (isJsonArray(keyPart)) {
            currentVal =
                findOrCreateJsonArray(currentVal, objKey, aryIdx).asValue();
            objKey = null;
            aryIdx = extractIndex(keyPart);
          } else { // JSON object
            if (flattened.get(key).isArray()) { // KEEP_ARRAYS mode
              flattened.set(key, unflattenArray(flattened.get(key).asArray()));
            }
            currentVal =
                findOrCreateJsonObject(currentVal, objKey, aryIdx).asValue();
            objKey = extractKey(keyPart);
            aryIdx = null;
          }
        }

        if (objKey == null && aryIdx == null) {
          if (isJsonArray(keyPart)) {
            aryIdx = extractIndex(keyPart);
            if (currentVal == null) currentVal = jsonCore.parse("[]").asValue();
          } else { // JSON object
            objKey = extractKey(keyPart);
            if (currentVal == null) currentVal = jsonCore.parse("{}").asValue();
          }
        }

        if (unflattened == null) unflattened = currentVal;
      }

      setUnflattenedValue(flattened, key, currentVal, objKey, aryIdx);
    }

    sw.append(writeByConfig(unflattened));
    return sw.toString();
  }

  /**
   * Returns a Java Map of nested objects by the given flattened JSON string.
   * 
   * @return a Java Map of nested objects
   */
  public Map<String, Object> unflattenAsMap() {
    JsonValueCore<?> flattenedValue = jsonCore.parse(unflatten());
    if (flattenedValue.isArray() || !flattenedValue.isObject()) {
      JsonObjectCore<?> jsonObj = jsonCore.parse("{}").asObject();
      jsonObj.set(ROOT, flattenedValue);
      return jsonObj.toMap();
    } else {
      return flattenedValue.asObject().toMap();
    }
  }

  private JsonArrayCore<?> unflattenArray(JsonArrayCore<?> array) {
    JsonArrayCore<?> unflattenArray = jsonCore.parse("[]").asArray();

    for (JsonValueCore<?> value : array) {
      if (value.isArray()) {
        unflattenArray.add(unflattenArray(value.asArray()));
      } else if (value.isObject()) {
        JsonValueCore<?> obj;
        obj = jsonCore.parse(newJsonUnflattener(value).unflatten());
        unflattenArray.add(obj);
      } else {
        unflattenArray.add(value);
      }
    }

    return unflattenArray;
  }

  private String extractKey(String keyPart) {
    if (keyPart.matches(objectComplexKey())) {
      keyPart = keyPart.replaceAll(
          "^" + Pattern.quote(leftBracket.toString()) + "\\s*\"", "");
      keyPart = keyPart.replaceAll(
          "\"\\s*" + Pattern.quote(rightBracket.toString()) + "$", "");
    }
    return keyTrans != null ? keyTrans.transform(keyPart) : keyPart;
  }

  private Integer extractIndex(String keyPart) {
    if (flattenMode.equals(MONGODB))
      return Integer.valueOf(keyPart);
    else
      return Integer.valueOf(
          keyPart.replaceAll("[" + Pattern.quote(leftBracket.toString())
              + Pattern.quote(rightBracket.toString()) + "\\s]", ""));
  }

  private boolean isJsonArray(String keyPart) {
    return keyPart.matches(arrayIndex())
        || (flattenMode.equals(MONGODB) && keyPart.matches("\\d+"));
  }

  private JsonArrayCore<?> findOrCreateJsonArray(JsonValueCore<?> currentVal,
      String objKey, Integer aryIdx) {
    if (objKey != null) {
      JsonObjectCore<?> jsonObj = currentVal.asObject();

      if (jsonObj.get(objKey) == null) {
        JsonArrayCore<?> ary = jsonCore.parse("[]").asArray();
        jsonObj.set(objKey, ary);

        return ary;
      }

      return jsonObj.get(objKey).asArray();
    } else { // aryIdx != null
      JsonArrayCore<?> jsonAry = currentVal.asArray();

      if (jsonAry.size() <= aryIdx || jsonAry.get(aryIdx).isNull()) {
        JsonArrayCore<?> ary = jsonCore.parse("[]").asArray();
        assureJsonArraySize(jsonAry, aryIdx);
        jsonAry.set(aryIdx, ary);

        return ary;
      }

      return jsonAry.get(aryIdx).asArray();
    }
  }

  private JsonObjectCore<?> findOrCreateJsonObject(JsonValueCore<?> currentVal,
      String objKey, Integer aryIdx) {
    if (objKey != null) {
      JsonObjectCore<?> jsonObj = currentVal.asObject();

      if (jsonObj.get(objKey) == null) {
        JsonObjectCore<?> obj = jsonCore.parse("{}").asObject();
        jsonObj.set(objKey, obj);

        return obj;
      }

      return jsonObj.get(objKey).asObject();
    } else { // aryIdx != null
      JsonArrayCore<?> jsonAry = currentVal.asArray();

      if (jsonAry.size() <= aryIdx || jsonAry.get(aryIdx).isNull()) {
        JsonObjectCore<?> obj = jsonCore.parse("{}").asObject();
        assureJsonArraySize(jsonAry, aryIdx);
        jsonAry.set(aryIdx, obj);

        return obj;
      }

      return jsonAry.get(aryIdx).asObject();
    }
  }

  private void setUnflattenedValue(JsonObjectCore<?> flattened, String key,
      JsonValueCore<?> currentVal, String objKey, Integer aryIdx) {
    JsonValueCore<?> val = flattened.get(key);
    if (objKey != null) {
      if (val.isArray()) {
        JsonArrayCore<?> jsonArray = jsonCore.parse("[]").asArray();
        for (JsonValueCore<?> arrayVal : val.asArray()) {
          jsonArray.add(parseJson(newJsonUnflattener(arrayVal).unflatten()));
        }
        currentVal.asObject().set(objKey, jsonArray);
      } else {
        currentVal.asObject().set(objKey, val);
      }
    } else { // aryIdx != null
      assureJsonArraySize(currentVal.asArray(), aryIdx);
      currentVal.asArray().set(aryIdx, val);
    }
  }

  private void assureJsonArraySize(JsonArrayCore<?> jsonArray, Integer index) {
    while (index >= jsonArray.size()) {
      jsonArray.add(jsonCore.parse("null"));
    }
  }

  @Override
  public int hashCode() {
    int result = 27;
    result = 31 * result + root.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JsonUnflattener)) return false;
    return root.equals(((JsonUnflattener) o).root);
  }

  @Override
  public String toString() {
    return "JsonUnflattener{root=" + root + "}";
  }

}
