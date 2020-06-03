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

import java.util.Objects;

import com.eclipsesource.json.JsonValue;
import com.github.wnameless.json.base.JsonValueBase;

public final class MinimalJsonValue implements JsonValueBase<MinimalJsonValue> {

  private final JsonValue jsonValue;

  public MinimalJsonValue(JsonValue jsonValue) {
    this.jsonValue = jsonValue;
  }

  @Override
  public boolean isObject() {
    return jsonValue.isObject();
  }

  @Override
  public boolean isArray() {
    return jsonValue.isArray();
  }

  @Override
  public boolean isNumber() {
    return jsonValue.isNumber();
  }

  @Override
  public boolean isString() {
    return jsonValue.isString();
  }

  @Override
  public boolean isBoolean() {
    return jsonValue.isBoolean();
  }

  @Override
  public boolean isNull() {
    return jsonValue.isNull();
  }

  @Override
  public MinimalJsonObject asObject() {
    return new MinimalJsonObject(jsonValue.asObject());
  }

  @Override
  public MinimalJsonArray asArray() {
    return new MinimalJsonArray(jsonValue.asArray());
  }

  @Override
  public int asInt() {
    return jsonValue.asInt();
  }

  @Override
  public long asLong() {
    return jsonValue.asLong();
  }

  @Override
  public double asDouble() {
    return jsonValue.asDouble();
  }

  @Override
  public String asString() {
    return jsonValue.asString();
  }

  @Override
  public boolean asBoolean() {
    return jsonValue.asBoolean();
  }

  @Override
  public int hashCode() {
    return jsonValue.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MinimalJsonValue)) return false;
    return Objects.equals(jsonValue, ((MinimalJsonValue) o).jsonValue);
  }

  @Override
  public String toString() {
    return jsonValue.toString();
  }

}
