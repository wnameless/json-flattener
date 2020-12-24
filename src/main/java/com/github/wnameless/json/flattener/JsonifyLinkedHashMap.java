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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.text.translate.CharSequenceTranslator;

import com.github.wnameless.json.base.JsonPrinter;

/**
 * {@link JsonifyLinkedHashMap} is simple a LinkedHashMap but with an override
 * jsonify toString method.
 * 
 * @author Wei-Ming Wu
 *
 * @param <K>
 *          the type of keys
 * @param <V>
 *          the type of values
 */
public class JsonifyLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

  private static final long serialVersionUID = 1L;

  private CharSequenceTranslator translator =
      StringEscapePolicy.DEFAULT.getCharSequenceTranslator();

  public JsonifyLinkedHashMap() {}

  public JsonifyLinkedHashMap(Map<K, V> map) {
    super(map);
  }

  public void setTranslator(CharSequenceTranslator translator) {
    this.translator = translator;
  }

  public String toString(PrintMode printMode) {
    switch (printMode) {
      case PRETTY:
        return JsonPrinter.prettyPrint(toString());
      default:
        return toString();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (Map.Entry<K, V> mem : entrySet()) {
      sb.append('"');
      sb.append(translator.translate((String) mem.getKey()));
      sb.append('"');
      sb.append(':');
      if (mem.getValue() instanceof String) {
        sb.append('"');
        sb.append(translator.translate((String) mem.getValue()));
        sb.append('"');
      } else if (mem.getValue() instanceof Collection) {
        sb.append(new JsonifyArrayList<>((Collection<?>) mem.getValue()));
      } else if (mem.getValue() instanceof Map) {
        sb.append(new JsonifyLinkedHashMap<>((Map<?, ?>) mem.getValue()));
      } else {
        sb.append(mem.getValue());
      }
      sb.append(',');
    }
    if (sb.length() > 1) sb.setLength(sb.length() - 1);
    sb.append('}');

    return sb.toString();
  }

}
