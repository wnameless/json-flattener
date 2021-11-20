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
package com.github.wnameless.json.flattener;

import static com.github.wnameless.json.flattener.FlattenMode.MONGODB;
import static com.github.wnameless.json.flattener.IndexedPeekIterator.newIndexedPeekIterator;
import static java.util.Collections.EMPTY_MAP;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.wnameless.json.base.JacksonJsonCore;
import com.github.wnameless.json.base.JsonCore;
import com.github.wnameless.json.base.JsonValueBase;
import com.github.wnameless.json.unflattener.JsonUnflattener;

/**
 * 
 * {@link JsonFlattener} flattens any JSON nested objects or arrays into a
 * flattened JSON string or a Map{@literal <Stirng, Object>}. The String key
 * will represents the corresponding position of value in the original nested
 * objects or arrays and the Object value are either String, Boolean, Long,
 * Double or null. <br>
 * <br>
 * For example:<br>
 * A nested JSON<br>
 * { "a" : { "b" : 1, "c": null, "d": [false, true] }, "e": "f", "g":2.3 }<br>
 * <br>
 * can be turned into a flattened JSON <br>
 * { "a.b": 1, "a.c": null, "a.d[0]": false, "a.d[1]": true, "e": "f", "g":2.3 }
 * <br>
 * <br>
 * or into a Map<br>
 * {<br>
 * &nbsp;&nbsp;a.b=1,<br>
 * &nbsp;&nbsp;a.c=null,<br>
 * &nbsp;&nbsp;a.d[0]=false,<br>
 * &nbsp;&nbsp;a.d[1]=true,<br>
 * &nbsp;&nbsp;e=f,<br>
 * &nbsp;&nbsp;g=2.3<br>
 * }
 *
 * @author Wei-Ming Wu
 *
 */
public final class JsonFlattener {

  /**
   * {@link ROOT} is the default key of the Map returned by
   * {@link #flattenAsMap}. When {@link JsonFlattener} processes a JSON string
   * which is not a JSON object or array, the final outcome may not suit in a
   * Java Map. At that moment, {@link JsonFlattener} will put the result in the
   * Map with {@link ROOT} as its key.
   */
  public static final String ROOT = "root";

  /**
   * Returns a flattened JSON string.
   * 
   * @param jsonVal
   *          a JSON data which wraps by {@link JsonValueBase}
   * @return a flattened JSON string
   */
  public static String flatten(JsonValueBase<?> jsonVal) {
    return new JsonFlattener(jsonVal).flatten();
  }

  /**
   * Returns a flattened JSON string.
   * 
   * @param json
   *          the JSON string
   * @return a flattened JSON string
   */
  public static String flatten(String json) {
    return new JsonFlattener(json).flatten();
  }

  /**
   * Returns a flattened JSON as Map.
   * 
   * @param jsonVal
   *          a JSON data which wraps by {@link JsonValueBase}
   * @return a flattened JSON as Map
   */
  public static Map<String, Object> flattenAsMap(JsonValueBase<?> jsonVal) {
    return new JsonFlattener(jsonVal).flattenAsMap();
  }

  /**
   * Returns a flattened JSON as Map.
   * 
   * @param json
   *          the JSON string
   * @return a flattened JSON as Map
   */
  public static Map<String, Object> flattenAsMap(String json) {
    return new JsonFlattener(json).flattenAsMap();
  }

  private final JsonCore<?> jsonCore;
  private final Deque<IndexedPeekIterator<?>> elementIters = new ArrayDeque<>();
  private final JsonValueBase<?> source;

  private JsonifyLinkedHashMap<String, Object> flattenedMap;

  private FlattenMode flattenMode = FlattenMode.NORMAL;
  private CharSequenceTranslatorFactory policy = StringEscapePolicy.DEFAULT;
  private Character separator = '.';
  private Character leftBracket = '[';
  private Character rightBracket = ']';
  private PrintMode printMode = PrintMode.MINIMAL;
  private KeyTransformer keyTrans = null;
  private boolean ignoreReservedCharacters = false;

  private JsonFlattener newJsonFlattener(JsonValueBase<?> jsonVal) {
    JsonFlattener jf = new JsonFlattener(jsonVal);
    jf.withFlattenMode(flattenMode);
    jf.withStringEscapePolicy(policy);
    jf.withSeparator(separator);
    jf.withLeftAndRightBrackets(leftBracket, rightBracket);
    jf.withPrintMode(printMode);
    if (keyTrans != null) jf.withKeyTransformer(keyTrans);
    if (ignoreReservedCharacters) jf.ignoreReservedCharacters();
    return jf;
  }

  /**
   * Creates a JSON flattener by given {@link JsonValueBase}.
   * 
   * @param json
   *          a {@link JsonValueBase}
   */
  public JsonFlattener(JsonValueBase<?> json) {
    jsonCore = new JacksonJsonCore();
    source = notNull(json);
  }

  /**
   * Creates a JSON flattener by given {@link JsonCore} and
   * {@link JsonValueBase}.
   * 
   * @param jsonCore
   *          a {@link JsonCore}
   * @param json
   *          a JSON string
   */
  public JsonFlattener(JsonCore<?> jsonCore, JsonValueBase<?> json) {
    this.jsonCore = notNull(jsonCore);
    source = jsonCore.parse(json.toJson());
  }

  /**
   * Creates a JSON flattener by given JSON string.
   * 
   * @param json
   *          a JSON string
   */
  public JsonFlattener(String json) {
    jsonCore = new JacksonJsonCore();
    source = jsonCore.parse(json);
  }

  /**
   * Creates a JSON flattener by given {@link JsonCore} and JSON string.
   * 
   * @param jsonCore
   *          a {@link JsonCore}
   * @param json
   *          a JSON string
   */
  public JsonFlattener(JsonCore<?> jsonCore, String json) {
    this.jsonCore = notNull(jsonCore);
    source = jsonCore.parse(json);
  }

  /**
   * Creates a JSON flattener by given JSON string reader.
   * 
   * @param jsonReader
   *          a JSON reader
   * @throws IOException
   *           if the jsonReader cannot be read
   */
  public JsonFlattener(Reader jsonReader) throws IOException {
    jsonCore = new JacksonJsonCore();
    source = jsonCore.parse(jsonReader);
  }

  /**
   * Creates a JSON flattener by given {@link JsonCore} and JSON string reader.
   * 
   * @param jsonCore
   *          a {@link JsonCore}
   * @param jsonReader
   *          a JSON string
   * @throws IOException
   *           if the jsonReader cannot be read
   */
  public JsonFlattener(JsonCore<?> jsonCore, Reader jsonReader)
      throws IOException {
    this.jsonCore = notNull(jsonCore);
    source = jsonCore.parse(jsonReader);
  }

  /**
   * A fluent setter to setup a mode of the {@link JsonFlattener}.
   * 
   * @param flattenMode
   *          a {@link FlattenMode}
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener withFlattenMode(FlattenMode flattenMode) {
    this.flattenMode = notNull(flattenMode);
    flattenedMap = null;
    return this;
  }

  /**
   * A fluent setter to setup the JSON string escape policy.
   * 
   * @param policy
   *          any {@link CharSequenceTranslatorFactory} or a
   *          {@link StringEscapePolicy}
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener withStringEscapePolicy(
      CharSequenceTranslatorFactory policy) {
    this.policy = notNull(policy);
    flattenedMap = null;
    return this;
  }

  /**
   * A fluent setter to setup the separator within a key in the flattened JSON.
   * The default separator is a dot(.).
   * 
   * @param separator
   *          any character
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener withSeparator(char separator) {
    String separatorStr = String.valueOf(separator);
    isTrue(!separatorStr.matches("[\"\\s]"),
        "Separator contains illegal character(%s)", separatorStr);
    isTrue(!leftBracket.equals(separator) && !rightBracket.equals(separator),
        "Separator(%s) is already used in brackets", separatorStr);

    this.separator = separator;
    flattenedMap = null;
    return this;
  }

  private String illegalBracketsRegex() {
    return "[\"\\s" + Pattern.quote(this.separator.toString()) + "]";
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
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener withLeftAndRightBrackets(char leftBracket,
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
    flattenedMap = null;
    return this;
  }

  /**
   * A fluent setter to setup a print mode of the {@link JsonFlattener}. The
   * default print mode is minimal.
   * 
   * @param printMode
   *          a {@link PrintMode}
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener withPrintMode(PrintMode printMode) {
    this.printMode = notNull(printMode);
    return this;
  }

  /**
   * A fluent setter to setup a {@link KeyTransformer} of the
   * {@link JsonFlattener}.
   * 
   * @param keyTrans
   *          a {@link KeyTransformer}
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener withKeyTransformer(KeyTransformer keyTrans) {
    this.keyTrans = notNull(keyTrans);
    flattenedMap = null;
    return this;
  }

  /**
   * After this option is enable, all reserved characters used in keys will stop
   * to be checked and escaped. <br>
   * <br>
   * Example:<br>
   * <br>
   * Input JSON: {"matrix":{"agent.smith":"1999"}}<br>
   * Flatten with option disable: {"matrix[\"agent.smith\"]":"1999"}<br>
   * Flatten with option enable: {"matrix.agent.smith":"1999"}<br>
   * <br>
   * {@link JsonUnflattener} may cause unpredictable results with the JSON
   * produced by a {@link JsonFlattener} with this option enable.
   * 
   * @return this {@link JsonFlattener}
   */
  public JsonFlattener ignoreReservedCharacters() {
    ignoreReservedCharacters = true;
    return this;
  }

  /**
   * Returns a flattened JSON string.
   * 
   * @return a flattened JSON string
   */
  public String flatten() {
    flattenAsMap();

    if (source.isObject() || isObjectifiableArray())
      return flattenedMap.toString(printMode);
    else
      return javaObj2Json(flattenedMap.get(ROOT));
  }

  private boolean isObjectifiableArray() {
    return source.isArray() && !flattenedMap.containsKey(ROOT);
  }

  private String javaObj2Json(Object obj) {
    if (obj == null) {
      return "null";
    } else if (obj instanceof CharSequence) {
      StringBuilder sb = new StringBuilder();
      sb.append('"');
      sb.append(
          policy.getCharSequenceTranslator().translate((CharSequence) obj));
      sb.append('"');
      return sb.toString();
    } else if (obj instanceof JsonifyArrayList) {
      JsonifyArrayList<?> list = (JsonifyArrayList<?>) obj;
      return list.toString(printMode);
    } else {
      return obj.toString();
    }
  }

  /**
   * Returns a flattened JSON as Map.
   * 
   * @return a flattened JSON as Map
   */
  public Map<String, Object> flattenAsMap() {
    if (flattenedMap != null) return flattenedMap;

    flattenedMap = newJsonifyLinkedHashMap();
    reduce(source);

    while (!elementIters.isEmpty()) {
      IndexedPeekIterator<?> deepestIter = elementIters.getLast();
      if (!deepestIter.hasNext()) {
        elementIters.removeLast();
      } else if (deepestIter.peek() instanceof Entry) {
        @SuppressWarnings("unchecked")
        Entry<String, ? extends JsonValueBase<?>> mem =
            (Entry<String, ? extends JsonValueBase<?>>) deepestIter.next();
        reduce(mem.getValue());
      } else { // JsonValue
        JsonValueBase<?> val = (JsonValueBase<?>) deepestIter.next();
        reduce(val);
      }
    }

    return flattenedMap;
  }

  private void reduce(JsonValueBase<?> val) {
    if (val.isObject() && val.asObject().iterator().hasNext()) {
      elementIters.add(newIndexedPeekIterator(val.asObject()));
    } else if (val.isArray() && val.asArray().iterator().hasNext()) {
      switch (flattenMode) {
        case KEEP_PRIMITIVE_ARRAYS:
          boolean allPrimitive = true;
          for (JsonValueBase<?> value : val.asArray()) {
            if (value.isArray() || value.isObject()) {
              allPrimitive = false;
              break;
            }
          }

          if (allPrimitive) {
            JsonifyArrayList<Object> array = newJsonifyArrayList();
            for (JsonValueBase<?> value : val.asArray()) {
              array.add(jsonVal2Obj(value));
            }
            flattenedMap.put(computeKey(), array);
          } else {
            elementIters.add(newIndexedPeekIterator(val.asArray()));
          }
          break;
        case KEEP_ARRAYS:
          JsonifyArrayList<Object> array = newJsonifyArrayList();
          for (JsonValueBase<?> value : val.asArray()) {
            array.add(jsonVal2Obj(value));
          }
          flattenedMap.put(computeKey(), array);
          break;
        default:
          elementIters.add(newIndexedPeekIterator(val.asArray()));
      }
    } else {
      String key = computeKey();
      Object value = jsonVal2Obj(val);
      // Check NOT empty JSON object
      if (!ROOT.equals(key) || !EMPTY_MAP.equals(value)) {
        flattenedMap.put(key, value);
      }
    }
  }

  private Object jsonVal2Obj(JsonValueBase<?> val) {
    if (val.isBoolean()) return val.asBoolean();
    if (val.isString()) return val.asString();
    if (val.isNumber()) return val.asNumber();

    switch (flattenMode) {
      case KEEP_ARRAYS:
        if (val.isArray()) {
          JsonifyArrayList<Object> array = newJsonifyArrayList();
          for (JsonValueBase<?> value : val.asArray()) {
            array.add(jsonVal2Obj(value));
          }
          return array;
        } else if (val.isObject()) {
          if (val.asObject().iterator().hasNext()) {
            return newJsonFlattener(val).flattenAsMap();
          } else {
            return newJsonifyLinkedHashMap();
          }
        }
      default:
        if (val.isArray()) {
          return newJsonifyArrayList();
        } else if (val.isObject()) {
          return newJsonifyLinkedHashMap();
        }
    }

    return null;
  }

  private boolean hasReservedCharacters(String key) {
    if (flattenMode.equals(MONGODB) && StringUtils.containsAny(key, separator))
      throw new IllegalArgumentException("Key cannot contain separator("
          + separator + ") in FlattenMode." + MONGODB);

    return StringUtils.containsAny(key, separator, leftBracket, rightBracket);
  }

  private String computeKey() {
    if (elementIters.isEmpty()) return ROOT;

    StringBuilder sb = new StringBuilder();

    for (IndexedPeekIterator<?> iter : elementIters) {
      if (iter.getCurrent() instanceof Entry) {
        @SuppressWarnings("unchecked")
        String key =
            ((Entry<String, ? extends JsonValueBase<?>>) iter.getCurrent())
                .getKey();
        if (keyTrans != null) key = keyTrans.transform(key);
        if (!ignoreReservedCharacters && hasReservedCharacters(key)) {
          sb.append(leftBracket);
          sb.append('"');
          sb.append(policy.getCharSequenceTranslator().translate(key));
          sb.append('"');
          sb.append(rightBracket);
        } else {
          if (sb.length() != 0) sb.append(separator);
          sb.append(key);
        }
      } else { // JsonValue
        sb.append(flattenMode.equals(MONGODB) ? separator : leftBracket);
        sb.append(iter.getIndex());
        sb.append(flattenMode.equals(MONGODB) ? "" : rightBracket);
      }
    }

    return sb.toString();
  }

  private <T> JsonifyArrayList<T> newJsonifyArrayList() {
    JsonifyArrayList<T> array = new JsonifyArrayList<>();
    array.setTranslator(policy.getCharSequenceTranslator());
    return array;
  }

  private <K, V> JsonifyLinkedHashMap<K, V> newJsonifyLinkedHashMap() {
    JsonifyLinkedHashMap<K, V> map = new JsonifyLinkedHashMap<>();
    map.setTranslator(policy.getCharSequenceTranslator());
    return map;
  }

  @Override
  public int hashCode() {
    int result = 27;
    result = 31 * result + source.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JsonFlattener)) return false;
    return source.equals(((JsonFlattener) o).source);
  }

  @Override
  public String toString() {
    return "JsonFlattener{source=" + source + "}";
  }

}
