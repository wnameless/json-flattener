/*
 *
 * Copyright 2019 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.json;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public final class JacksonJsonArray implements JsonArrayBase<JacksonJsonValue> {

  private final JsonNode jsonArray;

  public JacksonJsonArray(JsonNode jsonArray) {
    this.jsonArray = jsonArray;
  }

  @Override
  public JacksonJsonValue get(int index) {
    return new JacksonJsonValue(jsonArray.get(index));
  }

  @Override
  public int hashCode() {
    return jsonArray.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JacksonJsonArray)) return false;
    return jsonArray.equals(((JacksonJsonArray) o).jsonArray);
  }

  @Override
  public String toString() {
    return jsonArray.toString();
  }

  @Override
  public Iterator<JacksonJsonValue> iterator() {
    return new JacksonJsonValueIterator(jsonArray.iterator());
  }

  private class JacksonJsonValueIterator implements Iterator<JacksonJsonValue> {

    private final Iterator<JsonNode> jsonValueIterator;

    private JacksonJsonValueIterator(Iterator<JsonNode> jsonValueIterator) {
      this.jsonValueIterator = jsonValueIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonValueIterator.hasNext();
    }

    @Override
    public JacksonJsonValue next() {
      return new JacksonJsonValue(jsonValueIterator.next());
    }

  }

}
