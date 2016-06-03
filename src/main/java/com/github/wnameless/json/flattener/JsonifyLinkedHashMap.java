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

import java.util.LinkedHashMap;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public class JsonifyLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

  private static final long serialVersionUID = -8253975758958193883L;

  private final CharSequenceTranslator translator;

  public JsonifyLinkedHashMap(CharSequenceTranslator translator) {
    this.translator = translator;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    for (K key : keySet()) {
      sb.append("\"");
      sb.append(key);
      sb.append("\"");
      sb.append(":");
      V value = get(key);
      if (value instanceof String) {
        sb.append("\"");
        sb.append(translator.translate((String) value));
        sb.append("\"");
      } else {
        sb.append(value);
      }
      sb.append(",");
    }
    sb.setLength(sb.length() - 1);
    sb.append("}");

    return sb.toString();
  }

}
