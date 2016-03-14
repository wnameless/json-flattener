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

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

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

  private static final CharSequenceTranslator ESCAPE_JSON_WITHOUT_UNICODE =
      new AggregateTranslator(
          new LookupTranslator(new String[][] { { "\"", "\\\"" },
              { "\\", "\\\\" }, { "/", "\\/" } }),
          new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()));

  /**
   * Returns a flattened JSON string.
   * 
   * @param json
   *          the JSON string
   * @return a flattened JSON string.
   */
  public static String flatten(String json) {
    return new JsonFlattener(json).flatten();
  }

  /**
   * Returns a flattened JSON as Map.<br>
   * 
   * @param json
   *          the JSON string
   * @return a flattened JSON as Map
   */
  public static Map<String, Object> flattenAsMap(String json) {
    return new JsonFlattener(json).flattenAsMap();
  }

  private final JsonValue source;
  private final Deque<IndexedPeekIterator<?>> elementIters =
      new ArrayDeque<IndexedPeekIterator<?>>();
  private final Map<String, Object> flattenedJsonMap =
      new LinkedHashMap<String, Object>();
  private String flattenedJson = null;

  /**
   * Creates a JSON flattener.
   * 
   * @param json
   *          the JSON string
   */
  public JsonFlattener(String json) {
    source = Json.parse(json);
    if (!source.isObject() && !source.isArray()) {
      throw new IllegalArgumentException(
          "Input must be a JSON object or array");
    }

    if (source.isObject() && !source.asObject().iterator().hasNext()) {
      flattenedJson = "{}";
      return;
    }
    if (source.isArray() && !source.asArray().iterator().hasNext()) {
      flattenedJson = "[]";
      return;
    }

    reduce(source);
  }

  /**
   * Returns a flattened JSON string.
   * 
   * @return a flattened JSON string
   */
  public String flatten() {
    if (flattenedJson != null) return flattenedJson;

    flattenAsMap();

    StringBuilder sb = new StringBuilder("{");
    for (Entry<String, Object> mem : flattenedJsonMap.entrySet()) {
      String key = mem.getKey();
      Object val = mem.getValue();
      sb.append("\"");
      sb.append(key);
      sb.append("\"");
      sb.append(":");
      if (val instanceof Boolean) {
        sb.append(val);
      } else if (val instanceof String) {
        sb.append("\"");
        sb.append(ESCAPE_JSON_WITHOUT_UNICODE.translate((String) val));
        sb.append("\"");
      } else if (val instanceof BigDecimal) {
        sb.append(val);
      } else if (val instanceof List) {
        sb.append("[]");
      } else if (val instanceof Map) {
        sb.append("{}");
      } else {
        sb.append("null");
      }
      sb.append(",");
    }
    if (sb.length() > 1) sb.setLength(sb.length() - 1);
    sb.append("}");

    return flattenedJson = sb.toString();
  }

  /**
   * Returns a flattened JSON as Map.
   * 
   * @return a flattened JSON as Map
   */
  public Map<String, Object> flattenAsMap() {
    while (!elementIters.isEmpty()) {
      IndexedPeekIterator<?> deepestIter = elementIters.getLast();
      if (!deepestIter.hasNext()) {
        elementIters.removeLast();
      } else if (deepestIter.peek() instanceof Member) {
        Member mem = (Member) deepestIter.next();
        reduce(mem.getValue());
      } else { // JsonValue
        JsonValue val = (JsonValue) deepestIter.next();
        reduce(val);
      }
    }

    return flattenedJsonMap;
  }

  private void reduce(JsonValue val) {
    if (val.isObject() && val.asObject().iterator().hasNext())
      elementIters
          .add(new IndexedPeekIterator<Member>(val.asObject().iterator()));
    else if (val.isArray() && val.asArray().iterator().hasNext())
      elementIters
          .add(new IndexedPeekIterator<JsonValue>(val.asArray().iterator()));
    else
      flattenedJsonMap.put(computeKey(), jsonVal2Obj(val));
  }

  private Object jsonVal2Obj(JsonValue jsonValue) {
    if (jsonValue.isBoolean()) return jsonValue.asBoolean();
    if (jsonValue.isString()) return jsonValue.asString();
    if (jsonValue.isNumber()) return new BigDecimal(jsonValue.toString());
    if (jsonValue.isArray()) return new ArrayList<Object>();
    if (jsonValue.isObject()) return new LinkedHashMap<String, Object>();

    return null;
  }

  private String computeKey() {
    StringBuilder sb = new StringBuilder();

    for (IndexedPeekIterator<?> iter : elementIters) {
      if (iter.getCurrent() instanceof Member) {
        String key = ((Member) iter.getCurrent()).getName();
        if (key.contains(".")) {
          sb.append('[');
          sb.append('\\');
          sb.append('"');
          sb.append(ESCAPE_JSON_WITHOUT_UNICODE.translate(key));
          sb.append('\\');
          sb.append('"');
          sb.append(']');
        } else {
          if (sb.length() != 0) sb.append('.');
          sb.append(ESCAPE_JSON_WITHOUT_UNICODE.translate(key));
        }
      } else { // JsonValue
        sb.append('[');
        sb.append(iter.getIndex());
        sb.append(']');
      }
    }

    return sb.toString();
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
