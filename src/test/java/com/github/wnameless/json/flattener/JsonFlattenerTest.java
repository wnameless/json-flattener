/*
 *
 * Copyright 2015 Wei-Ming Wu
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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.base.JacksonJsonCore;
import com.github.wnameless.json.base.JacksonJsonValue;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

public class JsonFlattenerTest {

  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testConstructorException() {
    assertThrows(RuntimeException.class, () -> {
      new JsonFlattener("abc[123]}");
    });
  }

  @Test
  public void testFlatten() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals("{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}",
        JsonFlattener.flatten(json));

    assertEquals("{\"[0].a\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}",
        JsonFlattener.flatten("[{\"a\":1},2,{\"c\":[3,4]}]"));
  }

  @Test
  public void testFlattenAsMap() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals("{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}",
        JsonFlattener.flattenAsMap(json).toString());
  }

  @Test
  public void testFlattenWithJsonValueBase() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    JsonNode jsonVal = new ObjectMapper().readTree(json);
    assertEquals("{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}",
        JsonFlattener.flatten(new JacksonJsonValue(jsonVal)));

    assertEquals("{\"[0].a\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}",
        JsonFlattener.flatten("[{\"a\":1},2,{\"c\":[3,4]}]"));
  }

  @Test
  public void testFlattenAsMapWithJsonValueBase() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    JsonNode jsonVal = new ObjectMapper().readTree(json);
    assertEquals("{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}",
        JsonFlattener.flattenAsMap(new JacksonJsonValue(jsonVal)).toString());

    assertEquals("{\"[0].a\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}",
        JsonFlattener.flattenAsMap("[{\"a\":1},2,{\"c\":[3,4]}]").toString());
  }

  @Test
  public void testFlattenWithKeyContainsDotAndSquareBracket() throws IOException {
    assertEquals("{\"[0][\\\"a.a.[\\\"]\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}",
        JsonFlattener.flatten("[{\"a.a.[\":1},2,{\"c\":[3,4]}]"));
  }

  @Test
  public void testHashCode() throws IOException {
    URL url1 = Resources.getResource("test.json");
    String json1 = Resources.toString(url1, Charsets.UTF_8);
    URL url2 = Resources.getResource("test2.json");
    String json2 = Resources.toString(url2, Charsets.UTF_8);

    JsonFlattener flattener = new JsonFlattener(json1);
    assertEquals(flattener.hashCode(), flattener.hashCode());
    assertEquals(flattener.hashCode(), new JsonFlattener(json1).hashCode());
    assertNotEquals(flattener.hashCode(), new JsonFlattener(json2).hashCode());
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEquals() throws IOException {
    URL url1 = Resources.getResource("test.json");
    String json1 = Resources.toString(url1, Charsets.UTF_8);
    URL url2 = Resources.getResource("test2.json");
    String json2 = Resources.toString(url2, Charsets.UTF_8);

    JsonFlattener flattener = new JsonFlattener(json1);
    assertTrue(flattener.equals(flattener));
    assertTrue(flattener.equals(new JsonFlattener(json1)));
    assertFalse(flattener.equals(new JsonFlattener(json2)));
    assertFalse(flattener.equals(123L));
  }

  @Test
  public void testToString() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals(
        "JsonFlattener{source={\"a\":{\"b\":1,\"c\":null,\"d\":[false,true]},\"e\":\"f\",\"g\":2.3}}",
        new JsonFlattener(json).toString());
  }

  @Test
  public void testWithNoPrecisionDouble() throws IOException {
    String json = "{\"39473331\":{\"mega\":6.0,\"goals\":1.0}}";
    assertEquals("{\"39473331.mega\":6.0,\"39473331.goals\":1.0}",
        new JsonFlattener(json).flatten());
  }

  @Test
  public void testWithEmptyJsonObject() throws IOException {
    String json = "{}";
    assertEquals("{}", new JsonFlattener(json).flatten());
    assertEquals(json, JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
    assertEquals(newHashMap(), new JsonFlattener(json).flattenAsMap());
  }

  @Test
  public void testWithEmptyJsonArray() throws IOException {
    String json = "[]";
    assertEquals("[]", new JsonFlattener(json).flatten());
    assertEquals(ImmutableMap.of("root", newArrayList()), new JsonFlattener(json).flattenAsMap());
    assertEquals(json, JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
    assertEquals("[]", new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten());
    assertEquals(ImmutableMap.of("root", newArrayList()),
        new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).flattenAsMap());
    assertEquals(json, JsonUnflattener
        .unflatten(new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten()));
  }

  @Test
  public void testWithEmptyArray() {
    String json = "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[]}";
    assertEquals("{\"no\":\"1\",\"name\":\"riya\",\"marks\":[]}",
        new JsonFlattener(json).flatten());
    assertEquals(json, JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
  }

  @Test
  public void testWithEmptyObject() {
    String json = "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[{}]}";
    assertEquals("{\"no\":\"1\",\"name\":\"riya\",\"marks[0]\":{}}",
        new JsonFlattener(json).flatten());
    assertEquals(json, JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
  }

  @Test
  public void testWithArray() {
    String json = "[{\"abc\":123},456,[null]]";
    assertEquals("{\"[0].abc\":123,\"[1]\":456,\"[2][0]\":null}",
        new JsonFlattener(json).flatten());
    assertEquals(json, JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
  }

  @Test
  public void testWithSpecialCharacters() {
    String json = "[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]";
    assertEquals("{\"[0].abc\\t\":\" \\\" \\r \\t \1234 \"}", new JsonFlattener(json).flatten());
    json = "{\" \":[123,\"abc\"]}";
    assertEquals("{\" [0]\":123,\" [1]\":\"abc\"}", new JsonFlattener(json).flatten());
  }

  @Test
  public void testWithUnicodeCharacters() {
    String json = "[{\"姓名\":123}]";
    assertEquals("{\"[0].姓名\":123}", new JsonFlattener(json).flatten());
  }

  @Test
  public void testWithFlattenMode() throws IOException {
    URL url = Resources.getResource("test4.json");
    String json = Resources.toString(url, Charsets.UTF_8);
    assertEquals(
        "{\"a.b\":1,\"a.c\":null,\"a.d\":[false,{\"i.j\":[false,true,\"xy\"]}],\"e\":\"f\",\"g\":2.3,\"z\":[]}",
        new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testWithStringEscapePolicyALL_UNICODES() {
    String json = "{\"abc\":{\"def\":\"太極\"}}";
    assertEquals("{\"abc.def\":\"\\u592A\\u6975\"}",
        new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.ALL_UNICODES).flatten());
  }

  @Test
  public void testWithStringEscapePolicyALL() {
    String json = "{\"abc\":{\"def\":\"太極/兩儀\"}}";
    assertEquals("{\"abc.def\":\"\\u592A\\u6975\\/\\u5169\\u5100\"}",
        new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.ALL).flatten());
  }

  @Test
  public void testWithStringEscapePolicyALL_BUT_SLASH() {
    String json = "{\"abc\":{\"def\":\"太極/兩儀\"}}";
    assertEquals("{\"abc.def\":\"\\u592A\\u6975/\\u5169\\u5100\"}",
        new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.ALL_BUT_SLASH).flatten());
  }

  @Test
  public void testWithStringEscapePolicyALL_BUT_UNICODE() {
    String json = "{\"abc\":{\"def\":\"太極/兩儀\"}}";
    assertEquals("{\"abc.def\":\"太極\\/兩儀\"}", new JsonFlattener(json)
        .withStringEscapePolicy(StringEscapePolicy.ALL_BUT_UNICODE).flatten());
  }

  @Test
  public void testWithStringEscapePolicyALL_BUT_SLASH_AND_UNICODE() {
    String json = "{\"abc\":{\"def\":\"太極/兩儀\"}}";
    assertEquals("{\"abc.def\":\"太極/兩儀\"}", new JsonFlattener(json)
        .withStringEscapePolicy(StringEscapePolicy.ALL_BUT_SLASH_AND_UNICODE).flatten());
  }

  @Test
  public void testWithSeparator() {
    String json = "{\"abc\":{\"def\":123}}";
    assertEquals("{\"abc*def\":123}", new JsonFlattener(json).withSeparator('*').flatten());
  }

  @Test
  public void testWithSeparatorException() {
    String json = "{\"abc\":{\"def\":123}}";
    try {
      new JsonFlattener(json).withSeparator('"');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator contains illegal character(\")", e.getMessage());
    }
    try {
      new JsonFlattener(json).withSeparator(' ');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator contains illegal character( )", e.getMessage());
    }
    try {
      new JsonFlattener(json).withSeparator('[');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator([) is already used in brackets", e.getMessage());
    }
    try {
      new JsonFlattener(json).withSeparator(']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator(]) is already used in brackets", e.getMessage());
    }
  }

  @Test
  public void testWithLeftAndRightBracket() {
    String json = "{\"abc\":{\"A.\":[123,\"def\"]}}";
    assertEquals("{\"abc{\\\"A.\\\"}{0}\":123,\"abc{\\\"A.\\\"}{1}\":\"def\"}",
        new JsonFlattener(json).withLeftAndRightBrackets('{', '}').flatten());
  }

  @Test
  public void testWithLeftAndRightBracketsException() {
    String json = "{\"abc\":{\"def\":123}}";
    try {
      new JsonFlattener(json).withLeftAndRightBrackets('#', '#');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Both brackets cannot be the same", e.getMessage());
    }
    try {
      new JsonFlattener(json).withLeftAndRightBrackets('"', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character(\")", e.getMessage());
    }
    try {
      new JsonFlattener(json).withLeftAndRightBrackets(' ', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character( )", e.getMessage());
    }
    try {
      new JsonFlattener(json).withLeftAndRightBrackets('.', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character(.)", e.getMessage());
    }
    try {
      new JsonFlattener(json).withLeftAndRightBrackets('[', '"');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character(\")", e.getMessage());
    }
    try {
      new JsonFlattener(json).withLeftAndRightBrackets('[', ' ');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character( )", e.getMessage());
    }
    try {
      new JsonFlattener(json).withLeftAndRightBrackets('[', '.');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character(.)", e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRootInMap() {
    assertEquals("null", JsonFlattener.flatten("null"));
    assertEquals(null, JsonFlattener.flattenAsMap("null").get("root"));
    assertEquals("123", JsonFlattener.flatten("123"));
    assertEquals(123, JsonFlattener.flattenAsMap("123").get("root"));
    assertEquals("\"abc\"", JsonFlattener.flatten("\"abc\""));
    assertEquals("abc", JsonFlattener.flattenAsMap("\"abc\"").get("root"));
    assertEquals("true", JsonFlattener.flatten("true"));
    assertEquals(true, JsonFlattener.flattenAsMap("true").get("root"));
    assertEquals("[]", JsonFlattener.flatten("[]"));
    assertEquals(Collections.emptyList(), JsonFlattener.flattenAsMap("[]").get("root"));
    assertEquals("[[{\"abc.def\":123}]]", new JsonFlattener("[[{\"abc\":{\"def\":123}}]]")
        .withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten());
    List<List<Map<String, Object>>> root =
        (List<List<Map<String, Object>>>) new JsonFlattener("[[{\"abc\":{\"def\":123}}]]")
            .withFlattenMode(FlattenMode.KEEP_ARRAYS).flattenAsMap().get("root");
    assertEquals(ImmutableMap.of("abc.def", 123), root.get(0).get(0));
  }

  @Test
  public void testPrintMode() throws IOException {
    URL url = Resources.getResource("test.json");
    String src = Resources.toString(url, Charsets.UTF_8);

    String json = new JsonFlattener(src).withPrintMode(PrintMode.MINIMAL).flatten();
    assertEquals(mapper.readTree(json).toString(), json);

    json = new JsonFlattener(src).withPrintMode(PrintMode.PRETTY).flatten();
    assertEquals(mapper.readTree(json).toPrettyString(), json);

    src = "[[123]]";
    json = new JsonFlattener(src).withFlattenMode(FlattenMode.KEEP_ARRAYS)
        .withPrintMode(PrintMode.MINIMAL).flatten();
    assertEquals(mapper.readTree(json).toString(), json);

    json = new JsonFlattener(src).withFlattenMode(FlattenMode.KEEP_ARRAYS)
        .withPrintMode(PrintMode.PRETTY).flatten();
    assertEquals(mapper.readTree(json).toPrettyString(), json);
  }

  @Test
  public void testPrintModeWithEscapedDoubleQoutesAndBackslash() throws IOException {
    URL url = Resources.getResource("test_print_mode_unflatten_minimal.json");
    String input = Resources.toString(url, Charsets.UTF_8);
    JsonFlattener jf = new JsonFlattener(input);
    jf.withPrintMode(PrintMode.MINIMAL);
    String flattendJsonWithDotKey = jf.flatten();

    url = Resources.getResource("test_print_mode_flatten_minimal.json");
    String output = Resources.toString(url, Charsets.UTF_8);
    assertEquals(output, flattendJsonWithDotKey);

    jf.withPrintMode(PrintMode.PRETTY);
    flattendJsonWithDotKey = jf.flatten();

    url = Resources.getResource("test_print_mode_flatten_pretty.json");
    output = Resources.toString(url, Charsets.UTF_8);
    assertEquals(output, flattendJsonWithDotKey);

    JsonUnflattener ju = new JsonUnflattener(flattendJsonWithDotKey);
    ju.withPrintMode(PrintMode.MINIMAL);
    String nestedJsonWithDotKey = ju.unflatten();

    assertEquals(input, nestedJsonWithDotKey);

    ju.withPrintMode(PrintMode.PRETTY);
    nestedJsonWithDotKey = ju.unflatten();

    url = Resources.getResource("test_print_mode_unflatten_pretty.json");
    output = Resources.toString(url, Charsets.UTF_8);
    assertEquals(output, nestedJsonWithDotKey);
  }

  @Test
  public void testNoCache() {
    JsonFlattener jf = new JsonFlattener("{\"abc\":{\"def\":123}}");
    assertSame(jf.flattenAsMap(), jf.flattenAsMap());
    assertNotSame(jf.flatten(), jf.flatten());
    assertEquals("{\"abc*def\":123}", jf.withSeparator('*').flatten());
    assertEquals(jf.flatten(), jf.withPrintMode(PrintMode.MINIMAL).flatten());
  }

  @Test
  public void testNullPointerException() {
    try {
      new JsonFlattener("{\"abc\":{\"def\":123}}").withFlattenMode(null);
      fail();
    } catch (NullPointerException e) {
    }
    try {
      new JsonFlattener("{\"abc\":{\"def\":123}}").withStringEscapePolicy(null);
      fail();
    } catch (NullPointerException e) {
    }
    try {
      new JsonFlattener("{\"abc\":{\"def\":123}}").withPrintMode(null);
      fail();
    } catch (NullPointerException e) {
    }
  }

  @Test
  public void testFlattenWithNestedEmptyJsonObjectAndKeepArraysMode() throws IOException {
    URL url = Resources.getResource("test5.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals(
        "{\"a.b\":1,\"a.c\":null,\"a.d\":[false,{\"i.j\":[false,true]}],\"e\":\"f\",\"g\":2.3,\"z\":{}}",
        new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten());
  }

  @Test
  public void testWithSeparatorAndNestedObject() throws IOException {
    URL url = Resources.getResource("test5.json");
    String json = Resources.toString(url, Charsets.UTF_8);
    assertEquals(
        "{\"a_b\":1,\"a_c\":null,\"a_d\":[false,{\"i_j\":[false,true]}],\"e\":\"f\",\"g\":2.3,\"z\":{}}",
        new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).withSeparator('_')
            .flatten());
  }

  @Test
  public void testWithRootKeyInSourceObject() {
    String json = "{\"" + JsonFlattener.ROOT + "\":null, \"ss\":[123]}";
    assertEquals("{\"" + JsonFlattener.ROOT + "\":null,\"ss[0]\":123}",
        JsonFlattener.flatten(json));
  }

  @Test
  public void testInitByReader() throws IOException {
    URL url = Resources.getResource("test.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    JsonFlattener jf = new JsonFlattener(new InputStreamReader(url.openStream()));
    assertEquals(jf, new JsonFlattener(json));
  }

  @Test
  public void testFlattenModeMongodb() throws IOException {
    URL url = Resources.getResource("test_mongo.json");
    String src = Resources.toString(url, Charsets.UTF_8);

    URL urlMongo = Resources.getResource("test_mongo_flattened.json");
    String expectedJson = Resources.toString(urlMongo, Charsets.UTF_8);

    String flattened = new JsonFlattener(src).withFlattenMode(FlattenMode.MONGODB)
        .withPrintMode(PrintMode.PRETTY).flatten();

    assertEquals(expectedJson, flattened);
  }

  @Test
  public void testFlattenModeMongodbException() {
    String json = "{\"abc\":{\"de.f\":123}}";
    JsonFlattener jf = new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB);
    try {
      jf.flatten();
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Key cannot contain separator(.) in FlattenMode.MONGODB", e.getMessage());
    }
  }

  @Test
  public void testWithKeyTransformer() {
    String json = "{\"abc\":{\"de.f\":123}}";
    JsonFlattener jf = new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB)
        .withKeyTransformer(new KeyTransformer() {

          @Override
          public String transform(String key) {
            return key.replace('.', '_');
          }

        });
    assertEquals("{\"abc.de_f\":123}", jf.flatten());
  }

  @Test
  public void testWithFlattenModeKeepBottomArrays() throws IOException {
    URL url = Resources.getResource("test_keep_primitive_arrays.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    URL urlKBA = Resources.getResource("test_keep_primitive_arrays_flattened.json");
    String expectedJson = Resources.toString(urlKBA, Charsets.UTF_8);

    JsonFlattener jf = new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS)
        .withPrintMode(PrintMode.PRETTY);
    String flattened = jf.flatten();

    assertEquals(expectedJson, flattened);
  }

  @Test
  public void testWithJsonCore() throws IOException {
    URL url = Resources.getResource("test_long_decimal.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    ObjectMapper mapper = new ObjectMapper() {
      private static final long serialVersionUID = 1L;
      {
        configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
      }
    };

    JsonFlattener jf = new JsonFlattener(new JacksonJsonCore(mapper), json);
    jf.withPrintMode(PrintMode.PRETTY);
    assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json)),
        jf.flatten());

    jf = new JsonFlattener(new JacksonJsonCore(mapper), new StringReader(json));
    jf.withPrintMode(PrintMode.PRETTY);
    assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json)),
        jf.flatten());

    jf = new JsonFlattener(new JacksonJsonCore(mapper), new JacksonJsonCore(mapper).parse(json));
    jf.withPrintMode(PrintMode.PRETTY);
    assertEquals(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json)),
        jf.flatten());
  }

  @Test
  public void testWithIgnoreReservedCharacters() {
    String json = "{\"matrix\":{\"agent.smith\":\"1999\"}}";

    assertEquals("{\"matrix[\\\"agent.smith\\\"]\":\"1999\"}", JsonFlattener.flatten(json));
    assertEquals("{\"matrix.agent.smith\":\"1999\"}",
        new JsonFlattener(json).ignoreReservedCharacters().flatten());

    assertThrows(IllegalArgumentException.class, () -> {
      new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB).flatten();
    });
    assertEquals("{\"matrix.agent.smith\":\"1999\"}", new JsonFlattener(json)
        .withFlattenMode(FlattenMode.MONGODB).ignoreReservedCharacters().flatten());

    String jsonArray = "[{\"matrix\":\"reloaded\",\"agent\":{\"smith_no\":\"1\"}},"
        + "{\"matrix\":\"reloaded\",\"agent\":{\"smith_no\":\"2\"}}]";

    assertEquals(
        "{\"_0_matrix\":\"reloaded\",\"_0_agent_smith_no\":\"1\",\"_1_matrix\":\"reloaded\",\"_1_agent_smith_no\":\"2\"}",
        new JsonFlattener(jsonArray).withFlattenMode(FlattenMode.MONGODB).withSeparator('_')
            .ignoreReservedCharacters().flatten());
    assertEquals(
        "[{\"matrix\":\"reloaded\",\"agent_smith_no\":\"1\"},{\"matrix\":\"reloaded\",\"agent_smith_no\":\"2\"}]",
        new JsonFlattener(jsonArray).withFlattenMode(FlattenMode.KEEP_ARRAYS).withSeparator('_')
            .ignoreReservedCharacters().flatten());
  }

}
