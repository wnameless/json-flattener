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
package com.github.wnameless.json.base.ext;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.github.wnameless.json.base.JacksonJsonObject;
import com.github.wnameless.json.base.JsonObjectBase;

public final class MinimalJsonObject
    implements JsonObjectBase<MinimalJsonValue> {

  private final JsonObject jsonObject;

  public MinimalJsonObject(JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  @Override
  public MinimalJsonValue get(String name) {
    return new MinimalJsonValue(jsonObject.get(name));
  }

  @Override
  public int hashCode() {
    return jsonObject.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JacksonJsonObject)) return false;
    return Objects.equals(jsonObject, ((MinimalJsonObject) o).jsonObject);
  }

  @Override
  public String toString() {
    return jsonObject.toString();
  }

  @Override
  public Iterator<Entry<String, MinimalJsonValue>> iterator() {
    return new MinimalJsonEntryIterator(jsonObject.iterator());
  }

  private final class MinimalJsonEntryIterator
      implements Iterator<Entry<String, MinimalJsonValue>> {

    private final Iterator<Member> jsonMemberIterator;

    private MinimalJsonEntryIterator(Iterator<Member> jsonMemberIterator) {
      this.jsonMemberIterator = jsonMemberIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonMemberIterator.hasNext();
    }

    @Override
    public Entry<String, MinimalJsonValue> next() {
      Member member = jsonMemberIterator.next();
      return new AbstractMap.SimpleImmutableEntry<>(member.getName(),
          new MinimalJsonValue(member.getValue()));
    }

  }

}
