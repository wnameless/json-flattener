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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public final class GsonJsonArray implements JsonArrayBase<GsonJsonValue> {

  private final JsonArray jsonArray;

  public GsonJsonArray(JsonArray jsonArray) {
    this.jsonArray = jsonArray;
  }

  @Override
  public GsonJsonValue get(int index) {
    return new GsonJsonValue(jsonArray.get(index));
  }

  @Override
  public int hashCode() {
    return jsonArray.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GsonJsonArray)) return false;
    return jsonArray.equals(((GsonJsonArray) o).jsonArray);
  }

  @Override
  public String toString() {
    return jsonArray.toString();
  }

  @Override
  public Iterator<GsonJsonValue> iterator() {
    return new GsonJsonValueIterator(jsonArray.iterator());
  }

  private class GsonJsonValueIterator implements Iterator<GsonJsonValue> {

    private final Iterator<JsonElement> jsonValueIterator;

    private GsonJsonValueIterator(Iterator<JsonElement> jsonValueIterator) {
      this.jsonValueIterator = jsonValueIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonValueIterator.hasNext();
    }

    @Override
    public GsonJsonValue next() {
      return new GsonJsonValue(jsonValueIterator.next());
    }

  }

}
