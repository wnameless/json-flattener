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
package com.github.wnameless.json.flattener;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * 
 * {@link JsonFlattener} flatten any JSON nested objects or arrays into a simple
 * Map{@literal <Stirng, Object>}. The String key will represents the
 * corresponding position of value in the original nested objects or arrays and
 * the Object value are either String, Boolean, Number or null. <br>
 * <br>
 * For example: <br>
 * { "a" : { "b" : 1, "c": null, "d": [false, true] }, "e": "f"}<br>
 * will be turned into <br>
 * {<br>
 * a.b=1,<br>
 * a.c=null,<br>
 * a.d[0]=false,<br>
 * a.d[1]=true,<br>
 * e=f<br>
 * }
 *
 */
public final class JsonFlattener {

  private final JsonValue source;
  private final LinkedList<IndexedPeekIterator<?>> iters = newLinkedList();
  private final LinkedHashMap<String, Object> flattenJson = newLinkedHashMap();

  public JsonFlattener(String json) {
    source = Json.parse(json);
    if (!source.isObject() && !source.isArray())
      throw new IllegalArgumentException();

    if (source.isArray())
      iters.add(new IndexedPeekIterator<>((source.asArray().iterator())));
    if (source.isObject())
      iters.add(new IndexedPeekIterator<>(source.asObject().iterator()));
  }

  public Map<String, Object> flatten() {
    while (!iters.isEmpty()) {
      if (!iters.getLast().hasNext()) {
        iters.removeLast();
      } else if (iters.getLast().peek() instanceof Member) {
        Member mem = (Member) iters.getLast().next();
        JsonValue val = mem.getValue();
        reduce(val);
      } else if (iters.getLast().peek() instanceof JsonValue) {
        JsonValue val = (JsonValue) iters.getLast().next();
        reduce(val);
      }
    }

    return flattenJson;
  }

  private void reduce(JsonValue val) {
    if (val.isObject()) {
      iters.add(new IndexedPeekIterator<>(val.asObject().iterator()));
    } else if (val.isArray()) {
      iters.add(new IndexedPeekIterator<>(val.asArray().iterator()));
    } else {
      flattenJson.put(computeKey(), jsonVal2Obj(val));
    }
  }

  private Object jsonVal2Obj(JsonValue jsonValue) {
    if (jsonValue.isBoolean())
      return jsonValue.asBoolean();

    if (jsonValue.isString())
      return jsonValue.asString();

    if (jsonValue.isNumber()) {
      double v = jsonValue.asDouble();
      if ((v == Math.floor(v)) && !Double.isInfinite(v)) {
        return jsonValue.asLong();
      } else {
        return jsonValue.asDouble();
      }
    }

    return null;
  }

  private String computeKey() {
    String key = "";
    for (IndexedPeekIterator<?> iter : iters) {
      if (iter.getCurrent() instanceof Member) {
        if (!key.isEmpty())
          key += ".";
        key += ((Member) iter.getCurrent()).getName();
      } else {
        key += "[" + iter.getIndex() + "]";
      }
    }

    return key;
  }

  public static void main(String... args)
      throws IOException, URISyntaxException {
    URL url = Resources.getResource("test.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    System.out.println(new JsonFlattener(json).flatten());
  }

}
