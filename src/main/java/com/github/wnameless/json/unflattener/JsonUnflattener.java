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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.wnameless.json.flattener.FlattenMode;
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
   * Returns a JSON string of nested objects by the given flattened JSON string.
   * 
   * @param json
   *          a flattened JSON string
   * @return a JSON string of nested objects
   */
  public static String unflatten(String json) {
    return new JsonUnflattener(json).unflatten();
  }

  private final JsonNode root;

  private FlattenMode flattenMode = FlattenMode.NORMAL;
  private Character separator = '.';
  private Character leftBracket = '[';
  private Character rightBracket = ']';
  private PrintMode printMode = PrintMode.MINIMAL;
  private KeyTransformer keyTrans = null;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static JsonNode parseJson(String json) {
    try {
      return mapper.readTree(json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a JSON unflattener.
   * 
   * @param json
   *          the JSON string
   */
  public JsonUnflattener(String json) {
    root = parseJson(json);
  }

  /**
   * Creates a JSON unflattener.
   * 
   * @param jsonReader
   *          the JSON reader
   * @throws IOException
   *           if jsonReader cannot be read
   */
  public JsonUnflattener(Reader jsonReader) throws IOException {
    JsonNode jsonNode;
    try {
      jsonNode = mapper.readTree(jsonReader);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    root = jsonNode;
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

  @SuppressWarnings("deprecation")
  private String writeByConfig(JsonNode jsonNode) {
    switch (printMode) {
      case REGULAR:
        return jsonNode.toString();
      case PRETTY:
        return jsonNode.toPrettyString();
      default:
        return jsonNode.toString();
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
      ArrayNode unflattenedArray = unflattenArray((ArrayNode) root);
      sw.append(writeByConfig(unflattenedArray));
      return sw.toString();
    }
    if (!root.isObject()) {
      return root.toString();
    }

    ObjectNode flattened = (ObjectNode) root;
    JsonNode unflattened =
        flattened.isEmpty() ? JsonNodeFactory.instance.objectNode() : null;

    Iterator<String> names = flattened.fieldNames();
    while (names.hasNext()) {
      String key = names.next();
      JsonNode currentVal = unflattened;
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
              flattened.set(key,
                  unflattenArray((ArrayNode) flattened.get(key)));
            }
            currentVal = findOrCreateJsonObject(currentVal, objKey, aryIdx);
            objKey = extractKey(keyPart);
            aryIdx = null;
          }
        }

        if (objKey == null && aryIdx == null) {
          if (isJsonArray(keyPart)) {
            aryIdx = extractIndex(keyPart);
            if (currentVal == null) currentVal = JsonNodeFactory.instance.arrayNode();
          } else { // JSON object
            objKey = extractKey(keyPart);
            if (currentVal == null) currentVal = JsonNodeFactory.instance.objectNode();
          }
        }

        if (unflattened == null) unflattened = currentVal;
      }

      setUnflattenedValue(flattened, key, currentVal, objKey, aryIdx);
    }

    sw.append(writeByConfig(unflattened));
    return sw.toString();
  }

  private ArrayNode unflattenArray(ArrayNode array) {
    ArrayNode unflattenArray = JsonNodeFactory.instance.arrayNode();

    for (JsonNode value : array) {
      if (value.isArray()) {
        unflattenArray.add(unflattenArray((ArrayNode) value));
      } else if (value.isObject()) {
        JsonNode a = parseJson(new JsonUnflattener(value.toString())
            .withSeparator(separator).unflatten());
        unflattenArray.add(a);
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

  private JsonNode findOrCreateJsonArray(JsonNode currentVal, String objKey,
      Integer aryIdx) {
    if (objKey != null) {
      ObjectNode jsonObj = (ObjectNode) currentVal;

      if (jsonObj.get(objKey) == null) {
        ArrayNode ary = JsonNodeFactory.instance.arrayNode();
        jsonObj.set(objKey, ary);

        return ary;
      }

      return jsonObj.get(objKey);
    } else { // aryIdx != null
      ArrayNode jsonAry = (ArrayNode) currentVal;

      if (jsonAry.size() <= aryIdx
          || jsonAry.get(aryIdx).isNull()) {
        ArrayNode ary = JsonNodeFactory.instance.arrayNode();
        assureJsonArraySize(jsonAry, aryIdx);
        jsonAry.set(aryIdx, ary);

        return ary;
      }

      return jsonAry.get(aryIdx);
    }
  }

  private JsonNode findOrCreateJsonObject(JsonNode currentVal, String objKey,
      Integer aryIdx) {
    if (objKey != null) {
      ObjectNode jsonObj = (ObjectNode) currentVal;

      if (jsonObj.get(objKey) == null) {
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        jsonObj.set(objKey, obj);

        return obj;
      }

      return jsonObj.get(objKey);
    } else { // aryIdx != null
      ArrayNode jsonAry = (ArrayNode) currentVal;

      if (jsonAry.size() <= aryIdx
          || jsonAry.get(aryIdx).isNull()) {
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        assureJsonArraySize(jsonAry, aryIdx);
        jsonAry.set(aryIdx, obj);

        return obj;
      }

      return jsonAry.get(aryIdx);
    }
  }

  private void setUnflattenedValue(ObjectNode flattened, String key,
      JsonNode currentVal, String objKey, Integer aryIdx) {
    JsonNode val = flattened.get(key);
    if (objKey != null) {
      if (val.isArray()) {
        ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
        for (JsonNode arrayVal : (ArrayNode) val) {
          jsonArray.add(
              parseJson(newJsonUnflattener(arrayVal.toString()).unflatten()));
        }
        ((ObjectNode) currentVal).set(objKey, jsonArray);
      } else {
        ((ObjectNode) currentVal).set(objKey, val);
      }
    } else { // aryIdx != null
      assureJsonArraySize((ArrayNode) currentVal, aryIdx);
      ((ArrayNode) currentVal).set(aryIdx, val);
    }
  }

  private JsonUnflattener newJsonUnflattener(String json) {
    JsonUnflattener jf = new JsonUnflattener(json);
    if (flattenMode != null) jf.withFlattenMode(flattenMode);
    if (keyTrans != null) jf.withKeyTransformer(keyTrans);
    if (leftBracket != null && rightBracket != null)
      jf.withLeftAndRightBrackets(leftBracket, rightBracket);
    if (separator != null) jf.withSeparator(separator);
    return jf;
  }

  private void assureJsonArraySize(ArrayNode jsonArray, Integer index) {
    while (index >= jsonArray.size()) {
      jsonArray.add(JsonNodeFactory.instance.nullNode());
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
