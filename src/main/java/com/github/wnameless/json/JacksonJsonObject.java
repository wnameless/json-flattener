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

import com.fasterxml.jackson.databind.JsonNode;

public final class JacksonJsonObject
    implements JsonObjectBase<JacksonJsonValue> {

  private final JsonNode jsonObject;

  public JacksonJsonObject(JsonNode jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Override
  public JacksonJsonValue get(String name) {
    return new JacksonJsonValue(jsonObject.get(name));
  }

  @Override
  public int asInt() {
    return jsonObject.asInt();
  }

  @Override
  public long asLong() {
    return jsonObject.asLong();
  }

  @Override
  public double asDouble() {
    return jsonObject.asDouble();
  }

  @Override
  public boolean asBoolean() {
    return jsonObject.asBoolean();
  }

  @Override
  public String asString() {
    return jsonObject.asText();
  }

  @Override
  public Iterator<Entry<String, JacksonJsonValue>> iterator() {
    return new JacksonJsonEntryIterator(jsonObject.fields());
  }

  private final class JacksonJsonEntryIterator
      implements Iterator<Entry<String, JacksonJsonValue>> {

    private final Iterator<Entry<String, JsonNode>> jsonNodeIterator;

    private JacksonJsonEntryIterator(
        Iterator<Entry<String, JsonNode>> jsonNodeIterator) {
      this.jsonNodeIterator = jsonNodeIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonNodeIterator.hasNext();
    }

    @Override
    public Entry<String, JacksonJsonValue> next() {
      Entry<String, JsonNode> member = jsonNodeIterator.next();
      return new AbstractMap.SimpleImmutableEntry<>(member.getKey(),
          new JacksonJsonValue(member.getValue()));
    }

  }

}
