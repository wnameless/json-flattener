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

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class GsonJsonObject implements JsonObjectBase<GsonJsonValue> {

  private final JsonObject jsonObject;

  public GsonJsonObject(JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Override
  public GsonJsonValue get(String name) {
    return new GsonJsonValue(jsonObject.get(name));
  }

  @Override
  public int asInt() {
    return jsonObject.getAsInt();
  }

  @Override
  public long asLong() {
    return jsonObject.getAsLong();
  }

  @Override
  public double asDouble() {
    return jsonObject.getAsDouble();
  }

  @Override
  public boolean asBoolean() {
    return jsonObject.getAsBoolean();
  }

  @Override
  public String asString() {
    return jsonObject.getAsString();
  }

  @Override
  public int hashCode() {
    return jsonObject.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GsonJsonObject)) return false;
    return jsonObject.equals(((GsonJsonObject) o).jsonObject);
  }

  @Override
  public String toString() {
    return jsonObject.toString();
  }

  @Override
  public Iterator<Entry<String, GsonJsonValue>> iterator() {
    return new GsonJsonEntryIterator(jsonObject.entrySet().iterator());
  }

  private final class GsonJsonEntryIterator
      implements Iterator<Entry<String, GsonJsonValue>> {

    private final Iterator<Entry<String, JsonElement>> jsonElementIterator;

    private GsonJsonEntryIterator(
        Iterator<Entry<String, JsonElement>> jsonElementIterator) {
      this.jsonElementIterator = jsonElementIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonElementIterator.hasNext();
    }

    @Override
    public Entry<String, GsonJsonValue> next() {
      Entry<String, JsonElement> member = jsonElementIterator.next();
      return new AbstractMap.SimpleImmutableEntry<>(member.getKey(),
          new GsonJsonValue(member.getValue()));
    }

  }

}
