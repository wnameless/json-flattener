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

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.text.translate.CharSequenceTranslator;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.PrettyPrint;
import com.eclipsesource.json.WriterConfig;

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
      StringEscapePolicy.NORMAL.getCharSequenceTranslator();

  public void setTranslator(CharSequenceTranslator translator) {
    this.translator = translator;
  }

  public String toString(PrintMode printMode) {
    StringWriter sw = new StringWriter();
    try {
      switch (printMode) {
        case REGULAR:
          Json.parse(toString()).writeTo(sw, PrettyPrint.singleLine());
          break;
        case PRETTY:
          Json.parse(toString()).writeTo(sw, WriterConfig.PRETTY_PRINT);
          break;
        default:
          return toString();
      }
    } catch (IOException e) {}

    return sw.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (Entry<K, V> mem : entrySet()) {
      sb.append('"');
      sb.append(mem.getKey());
      sb.append('"');
      sb.append(':');
      if (mem.getValue() instanceof String) {
        sb.append('"');
        sb.append(translator.translate((String) mem.getValue()));
        sb.append('"');
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
