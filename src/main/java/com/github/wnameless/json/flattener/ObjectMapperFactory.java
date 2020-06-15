/*
 *
 * Copyright 2020 Wei-Ming Wu
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

import java.io.IOException;

import org.apache.commons.text.translate.CharSequenceTranslator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

class ObjectMapperFactory {

  private static final ObjectMapperFactory INSTANCE = new ObjectMapperFactory();

  private final ObjectMapper mapper;

  private ObjectMapperFactory() {
    mapper = new ObjectMapper();
  }

  public static ObjectMapper get() {
    return INSTANCE.mapper;
  }

  public static ObjectMapper getWriter() {
    ObjectMapper mapper = new ObjectMapper();

    JsonSerializer<String> js = new JsonSerializer<>() {

      @Override
      public void serialize(String value, JsonGenerator gen,
          SerializerProvider serializers) throws IOException {
        gen.writeRawValue("\"" + value + "\"");
      }

    };
    SimpleModule module = new SimpleModule();
    module.addSerializer(String.class, js);
    mapper.registerModule(module);

    return mapper;
  }

  public static ObjectMapper get(CharSequenceTranslator translator) {
    ObjectMapper mapper = new ObjectMapper();

    SimpleModule module = new SimpleModule();
    module.addDeserializer(String.class, getDeserializer(translator));
    mapper.registerModule(module);

    return mapper;
  }

  private static JsonDeserializer<String> getDeserializer(
      CharSequenceTranslator translator) {
    return new JsonDeserializer<String>() {

      @Override
      public String deserialize(JsonParser p, DeserializationContext ctxt)
          throws IOException, JsonProcessingException {
        return translator.translate(p.getValueAsString());
      }

    };
  }

}
