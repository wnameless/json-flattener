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
package com.github.wnameless.json.unflattener;

import static org.apache.commons.lang3.Validate.notNull;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import com.github.wnameless.json.base.JsonCore;

/**
 * 
 * {@link JsonUnflattenerFactory} preserves the configuration of a {@link JsonUnflattener}, in doing
 * so, any input JSON data can be used to create a {@link JsonUnflattener} object with the same
 * configuration.
 * 
 * @author Wei-Ming Wu
 *
 */
public final class JsonUnflattenerFactory {

  private final Consumer<JsonUnflattener> configurer;
  private final Optional<JsonCore<?>> jsonCore;

  /**
   * Returns a {@link JsonUnflattenerFactory}.
   * 
   * @param configurer a functional interface used to set up the configuration of a
   *        {@link JsonUnflattener}.
   */
  public JsonUnflattenerFactory(Consumer<JsonUnflattener> configurer) {
    this.configurer = notNull(configurer);
    this.jsonCore = Optional.empty();
  }

  /**
   * Returns a {@link JsonUnflattenerFactory}.
   * 
   * @param configurer a functional interface used to set up the configuration of a
   *        {@link JsonUnflattener}.
   * @param jsonCore a {@link JsonCore}
   */
  public JsonUnflattenerFactory(Consumer<JsonUnflattener> configurer, JsonCore<?> jsonCore) {
    this.configurer = notNull(configurer);
    this.jsonCore = Optional.of(jsonCore);
  }

  /**
   * Creates a {@link JsonUnflattener} by given JSON string and configures it with the configurer
   * and jsonCore within this {@link JsonUnflattenerFactory}.
   * 
   * @param json the JSON string
   * @return a {@link JsonUnflattener}
   */
  public JsonUnflattener build(String json) {
    JsonUnflattener jf;
    if (jsonCore.isPresent()) {
      jf = new JsonUnflattener(jsonCore.get(), json);
    } else {
      jf = new JsonUnflattener(json);
    }

    configurer.accept(jf);
    return jf;
  }

  /**
   * Creates a {@link JsonUnflattener} by given flattened {@link Map} and configures it with the
   * configurer and jsonCore within this {@link JsonUnflattenerFactory}.
   * 
   * @param flattenedMap a flattened {@link Map}
   * @return a {@link JsonUnflattener}
   */
  public JsonUnflattener build(Map<String, ?> flattenedMap) {
    JsonUnflattener jf;
    if (jsonCore.isPresent()) {
      jf = new JsonUnflattener(jsonCore.get(), flattenedMap);
    } else {
      jf = new JsonUnflattener(flattenedMap);
    }
    configurer.accept(jf);
    return jf;
  }

  /**
   * Creates a {@link JsonUnflattener} by given JSON reader and configures it with the configurer
   * and jsonCore within this {@link JsonUnflattenerFactory}.
   * 
   * @param jsonReader a JSON reader
   * @return a {@link JsonUnflattener}
   * @throws IOException if the jsonReader cannot be read
   */
  public JsonUnflattener build(Reader jsonReader) throws IOException {
    JsonUnflattener jf;
    if (jsonCore.isPresent()) {
      jf = new JsonUnflattener(jsonCore.get(), jsonReader);
    } else {
      jf = new JsonUnflattener(jsonReader);
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
    if (!(o instanceof JsonUnflattenerFactory)) return false;
    JsonUnflattenerFactory other = (JsonUnflattenerFactory) o;
    return configurer.equals(other.configurer) && jsonCore.equals(other.jsonCore);
  }

  @Override
  public String toString() {
    return "JsonUnflattenerFactory{configurer=" + configurer + ", jsonCore=" + jsonCore + "}";
  }

}
