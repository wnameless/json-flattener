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

import com.fasterxml.jackson.databind.JsonNode;

public final class JacksonJsonValue implements JsonValueBase {

  private final JsonNode jsonValue;

  public JacksonJsonValue(JsonNode jsonValue) {
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
    return jsonValue.isTextual();
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
  public JacksonJsonObject asObject() {
    return new JacksonJsonObject(jsonValue);
  }

  @Override
  public JacksonJsonArray asArray() {
    return new JacksonJsonArray(jsonValue);
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
    return jsonValue.asText();
  }

  @Override
  public boolean asBoolean() {
    return jsonValue.asBoolean();
  }

}
