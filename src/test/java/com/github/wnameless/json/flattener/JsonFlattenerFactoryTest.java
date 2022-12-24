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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.wnameless.json.base.GsonJsonCore;
import com.github.wnameless.json.base.JacksonJsonCore;
import com.github.wnameless.json.base.JsonCore;
import com.github.wnameless.json.base.JsonPrinter;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JsonFlattenerFactoryTest {

  Consumer<JsonFlattener> configurer;
  JsonCore<?> jsonCore;
  JsonFlattenerFactory jsonFlattenerFactory;

  @BeforeEach
  public void setUp() {
    configurer = jf -> jf.withPrintMode(PrintMode.PRETTY);
    jsonCore = new GsonJsonCore();
    jsonFlattenerFactory = new JsonFlattenerFactory(configurer, jsonCore);
  }

  @Test
  public void testBuildWithJSONString() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    JsonFlattener jf = jsonFlattenerFactory.build(json);
    assertEquals(
        JsonPrinter.prettyPrint(
            "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"),
        jf.flatten());

    jsonFlattenerFactory = new JsonFlattenerFactory(configurer);
    jf = jsonFlattenerFactory.build(json);
    assertEquals(
        JsonPrinter.prettyPrint(
            "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"),
        jf.flatten());
  }

  @Test
  public void testBuildWithJsonValueBase() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    JsonFlattener jf = jsonFlattenerFactory.build(jsonCore.parse(json));
    assertEquals(
        JsonPrinter.prettyPrint(
            "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"),
        jf.flatten());

    jsonFlattenerFactory = new JsonFlattenerFactory(configurer);
    jf = jsonFlattenerFactory.build(jsonCore.parse(json));
    assertEquals(
        JsonPrinter.prettyPrint(
            "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"),
        jf.flatten());
  }

  @Test
  public void testBuildWithJsonReader() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    JsonFlattener jf = jsonFlattenerFactory.build(new StringReader(json));
    assertEquals(
        JsonPrinter.prettyPrint(
            "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"),
        jf.flatten());

    jsonFlattenerFactory = new JsonFlattenerFactory(configurer);
    jf = jsonFlattenerFactory.build(new StringReader(json));
    assertEquals(
        JsonPrinter.prettyPrint(
            "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}"),
        jf.flatten());
  }

  @Test
  public void testHashCode() {
    int result = 27;
    result = 31 * result + configurer.hashCode();
    result = 31 * result + jsonCore.hashCode();
    assertEquals(result, jsonFlattenerFactory.hashCode());

    configurer = jf -> jf.withPrintMode(PrintMode.PRETTY);
    jsonCore = new JacksonJsonCore();
    jsonFlattenerFactory = new JsonFlattenerFactory(configurer, jsonCore);
    assertNotEquals(result, jsonFlattenerFactory.hashCode());
  }

  @Test
  public void testEquals() {
    assertEquals(jsonFlattenerFactory, jsonFlattenerFactory);

    JsonFlattenerFactory otherJsonFlattenerFactory = new JsonFlattenerFactory(configurer, jsonCore);
    assertEquals(jsonFlattenerFactory, otherJsonFlattenerFactory);

    otherJsonFlattenerFactory = new JsonFlattenerFactory(configurer);
    assertNotEquals(jsonFlattenerFactory, otherJsonFlattenerFactory);

    jsonCore = new JacksonJsonCore();
    otherJsonFlattenerFactory = new JsonFlattenerFactory(configurer, jsonCore);
    assertNotEquals(jsonFlattenerFactory, otherJsonFlattenerFactory);

    configurer = jf -> jf.withPrintMode(PrintMode.MINIMAL);
    otherJsonFlattenerFactory = new JsonFlattenerFactory(configurer, jsonCore);
    assertNotEquals(jsonFlattenerFactory, otherJsonFlattenerFactory);

    assertNotEquals(jsonFlattenerFactory, null);
  }

  @Test
  public void testToString() {
    jsonFlattenerFactory = new JsonFlattenerFactory(configurer);

    assertEquals("JsonFlattenerFactory{configurer=" + configurer.toString() + ", jsonCore="
        + Optional.empty() + "}", jsonFlattenerFactory.toString());

    jsonCore = new GsonJsonCore();
    jsonFlattenerFactory = new JsonFlattenerFactory(configurer, jsonCore);
    assertEquals("JsonFlattenerFactory{configurer=" + configurer.toString() + ", jsonCore="
        + Optional.of(jsonCore) + "}", jsonFlattenerFactory.toString());
  }

}
