/*
 *
 * Copyright 2022 Wei-Ming Wu
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
package com.github.wnameless.json.flattener;

import static org.apache.commons.lang3.Validate.notNull;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.function.Consumer;
import com.github.wnameless.json.base.JsonCore;
import com.github.wnameless.json.base.JsonValueBase;

/**
 * 
 * {@link JsonFlattenerFactory} preserves the configuration of a {@link JsonFlattener}, in doing so,
 * any input JSON data can be used to create a {@link JsonFlattener} object with the same
 * configuration.
 *
 * @author Wei-Ming Wu
 * 
 */
public final class JsonFlattenerFactory {

  private final Consumer<JsonFlattener> configurer;
  private final Optional<JsonCore<?>> jsonCore;

  /**
   * Returns a {@link JsonFlattenerFactory}.
   * 
   * @param configurer a functional interface used to set up the configuration of a
   *        {@link JsonFlattener}.
   */
  public JsonFlattenerFactory(Consumer<JsonFlattener> configurer) {
    this.configurer = notNull(configurer);
    this.jsonCore = Optional.empty();
  }

  /**
   * Returns a {@link JsonFlattenerFactory}.
   * 
   * @param configurer a functional interface used to set up the configuration of a
   *        {@link JsonFlattener}.
   * @param jsonCore a {@link JsonCore}
   */
  public JsonFlattenerFactory(Consumer<JsonFlattener> configurer, JsonCore<?> jsonCore) {
    this.configurer = notNull(configurer);
    this.jsonCore = Optional.of(jsonCore);
  }

  /**
   * Creates a {@link JsonFlattener} by given JSON string and configures it with the configurer and
   * jsonCore within this {@link JsonFlattenerFactory}.
   * 
   * @param json the JSON string
   * @return a {@link JsonFlattener}
   */
  public JsonFlattener build(String json) {
    JsonFlattener jf;
    if (jsonCore.isPresent()) {
      jf = new JsonFlattener(jsonCore.get(), json);
    } else {
      jf = new JsonFlattener(json);
    }
    configurer.accept(jf);
    return jf;
  }

  /**
   * Creates a {@link JsonFlattener} by given {@link JsonValueBase} and configures it with the
   * configurer and jsonCore within this {@link JsonFlattenerFactory}.
   * 
   * @param json the {@link JsonValueBase}
   * @return a {@link JsonFlattener}
   */
  public JsonFlattener build(JsonValueBase<?> json) {
    JsonFlattener jf;
    if (jsonCore.isPresent()) {
      jf = new JsonFlattener(jsonCore.get(), json);
    } else {
      jf = new JsonFlattener(json);
    }
    configurer.accept(jf);
    return jf;
  }

  /**
   * Creates a {@link JsonFlattener} by given JSON reader and configures it with the configurer and
   * jsonCore within this {@link JsonFlattenerFactory}.
   * 
   * @param jsonReader a JSON reader
   * @return a {@link JsonFlattener}
   * @throws IOException if the jsonReader cannot be read
   */
  public JsonFlattener build(Reader jsonReader) throws IOException {
    JsonFlattener jf;
    if (jsonCore.isPresent()) {
      jf = new JsonFlattener(jsonCore.get(), jsonReader);
    } else {
      jf = new JsonFlattener(jsonReader);
    }
    configurer.accept(jf);
    return jf;
  }

  @Override
  public int hashCode() {
    int result = 27;
    result = 31 * result + configurer.hashCode();
    result = 31 * result + jsonCore.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JsonFlattenerFactory)) return false;
    JsonFlattenerFactory other = (JsonFlattenerFactory) o;
    return configurer.equals(other.configurer) && jsonCore.equals(other.jsonCore);
  }

  @Override
  public String toString() {
    return "JsonFlattenerFactory{configurer=" + configurer + ", jsonCore=" + jsonCore + "}";
  }

}
