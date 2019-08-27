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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

public final class MinimalJsonArray implements JsonArrayBase<MinimalJsonValue> {

  private final JsonArray jsonArray;

  public MinimalJsonArray(JsonArray jsonArray) {
    this.jsonArray = jsonArray;
  }

  @Override
  public MinimalJsonValue get(int index) {
    return new MinimalJsonValue(jsonArray.get(index));
  }

  @Override
  public int hashCode() {
    return jsonArray.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MinimalJsonArray)) return false;
    return jsonArray.equals(((MinimalJsonArray) o).jsonArray);
  }

  @Override
  public String toString() {
    return jsonArray.toString();
  }

  @Override
  public Iterator<MinimalJsonValue> iterator() {
    return new MinimalJsonValueIterator(jsonArray.iterator());
  }

  private class MinimalJsonValueIterator implements Iterator<MinimalJsonValue> {

    private final Iterator<JsonValue> jsonValueIterator;

    private MinimalJsonValueIterator(Iterator<JsonValue> jsonValueIterator) {
      this.jsonValueIterator = jsonValueIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonValueIterator.hasNext();
    }

    @Override
    public MinimalJsonValue next() {
      return new MinimalJsonValue(jsonValueIterator.next());
    }

  }

}
