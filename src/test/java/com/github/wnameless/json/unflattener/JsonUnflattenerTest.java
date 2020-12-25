/*
 *
 * Copyright 2015 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.json.unflattener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.base.JacksonJsonCore;
import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.flattener.KeyTransformer;
import com.github.wnameless.json.flattener.PrintMode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JsonUnflattenerTest {

  ObjectMapper mapper = new ObjectMapper();

  private Map<String, ?> toMap(String json)
      throws JsonMappingException, JsonProcessingException {
    return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
  }

  private Map<String, ?> toRootMap(String json)
      throws JsonMappingException, JsonProcessingException {
    return mapper.readValue("{\"" + JsonUnflattener.ROOT + "\":" + json + "}",
        new TypeReference<Map<String, Object>>() {});
  }

  @Test
  public void testConstructorException() {
    assertThrows(RuntimeException.class, () -> {
      new JsonUnflattener("abc[123]}");
    });
  }

  @Test
  public void testUnflatten()
      throws JsonMappingException, JsonProcessingException {
    assertEquals(
        "{\"a\":{\"b\":1,\"c\":null,\"d\":[false,true,{\"sss\":777,\"vvv\":888}]},\"e\":\"f\",\"g\":2.3}",
        JsonUnflattener.unflatten(
            "{\"a.b\":1,\"a.c\":null,\"a.d[1]\":true,\"a.d[0]\":false,\"a.d[2].sss\":777,\"a.d[2].vvv\":888,\"e\":\"f\",\"g\":2.3}"));

    assertEquals("[1,[2,3],4,{\"abc\":5}]", JsonUnflattener.unflatten(
        "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3].abc\":5}"));

    assertEquals("{\" \\\"abc\":{\"def \":123}}", JsonUnflattener
        .unflatten(JsonFlattener.flatten("{\" \\\"abc\":{\"def \":123}}")));

    assertEquals("{\" ].$f\":{\"abc\":{\"def\":[123]}}}",
        JsonUnflattener.unflatten("{\"[\\\" ].$f\\\"].abc.def[0]\":123}"));

    assertEquals("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]",
        JsonUnflattener.unflatten(
            JsonFlattener.flatten("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]")));

    // Map
    assertEquals(
        "{\"a\":{\"b\":1,\"c\":null,\"d\":[false,true,{\"sss\":777,\"vvv\":888}]},\"e\":\"f\",\"g\":2.3}",
        JsonUnflattener.unflatten(toMap(
            "{\"a.b\":1,\"a.c\":null,\"a.d[1]\":true,\"a.d[0]\":false,\"a.d[2].sss\":777,\"a.d[2].vvv\":888,\"e\":\"f\",\"g\":2.3}")));

    assertEquals("[1,[2,3],4,{\"abc\":5}]", JsonUnflattener.unflatten(toMap(
        "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3].abc\":5}")));

    assertEquals("{\" \\\"abc\":{\"def \":123}}", JsonUnflattener.unflatten(
        toMap(JsonFlattener.flatten("{\" \\\"abc\":{\"def \":123}}"))));

    assertEquals("{\" ].$f\":{\"abc\":{\"def\":[123]}}}", JsonUnflattener
        .unflatten(toMap("{\"[\\\" ].$f\\\"].abc.def[0]\":123}")));

    assertEquals("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]",
        JsonUnflattener.unflatten(toMap(
            JsonFlattener.flatten("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]"))));
  }

  @Test
  public void testUnflattenAsMap()
      throws JsonMappingException, JsonProcessingException {
    assertEquals(toMap(
        "{\"a\":{\"b\":1,\"c\":null,\"d\":[false,true,{\"sss\":777,\"vvv\":888}]},\"e\":\"f\",\"g\":2.3}"),
        JsonUnflattener.unflattenAsMap(
            "{\"a.b\":1,\"a.c\":null,\"a.d[1]\":true,\"a.d[0]\":false,\"a.d[2].sss\":777,\"a.d[2].vvv\":888,\"e\":\"f\",\"g\":2.3}"));

    assertEquals(toRootMap("[1,[2,3],4,{\"abc\":5}]"),
        JsonUnflattener.unflattenAsMap(
            "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3].abc\":5}"));

    assertEquals(toMap("{\" \\\"abc\":{\"def \":123}}"),
        JsonUnflattener.unflattenAsMap(
            JsonFlattener.flatten("{\" \\\"abc\":{\"def \":123}}")));

    assertEquals(toMap("{\" ].$f\":{\"abc\":{\"def\":[123]}}}"),
        JsonUnflattener.unflattenAsMap("{\"[\\\" ].$f\\\"].abc.def[0]\":123}"));

    assertEquals(toRootMap("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]"),
        JsonUnflattener.unflattenAsMap(
            JsonFlattener.flatten("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]")));

    // Map
    assertEquals(toMap(
        "{\"a\":{\"b\":1,\"c\":null,\"d\":[false,true,{\"sss\":777,\"vvv\":888}]},\"e\":\"f\",\"g\":2.3}"),
        JsonUnflattener.unflattenAsMap(toMap(
            "{\"a.b\":1,\"a.c\":null,\"a.d[1]\":true,\"a.d[0]\":false,\"a.d[2].sss\":777,\"a.d[2].vvv\":888,\"e\":\"f\",\"g\":2.3}")));

    assertEquals(toRootMap("[1,[2,3],4,{\"abc\":5}]"),
        JsonUnflattener.unflattenAsMap(toMap(
            "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3].abc\":5}")));

    assertEquals(toMap("{\" \\\"abc\":{\"def \":123}}"),
        JsonUnflattener.unflattenAsMap(
            toMap(JsonFlattener.flatten("{\" \\\"abc\":{\"def \":123}}"))));

    assertEquals(toMap("{\" ].$f\":{\"abc\":{\"def\":[123]}}}"), JsonUnflattener
        .unflattenAsMap(toMap("{\"[\\\" ].$f\\\"].abc.def[0]\":123}")));

    assertEquals(toRootMap("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]"),
        JsonUnflattener.unflattenAsMap(toMap(
            JsonFlattener.flatten("[{\"abc\\t\":\" \\\" \\r \\t \1234 \"}]"))));
  }

  @Test
  public void testUnflattenWithArrayOfNestedObjectsInValByKeepArraysMode()
      throws IOException {
    URL url = Resources.getResource("test6.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    String flattendJson = new JsonFlattener(json)
        .withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten();
    assertEquals("{\"a\":[1,2,3],\"b\":[{\"c.d\":[1,2]}]}", flattendJson);
    assertEquals("{\"a\":[1,2,3],\"b\":[{\"c\":{\"d\":[1,2]}}]}",
        JsonUnflattener.unflatten(flattendJson));

    // Map
    Map<String, Object> flattendMap = new JsonFlattener(json)
        .withFlattenMode(FlattenMode.KEEP_ARRAYS).flattenAsMap();
    assertEquals("{\"a\":[1,2,3],\"b\":[{\"c.d\":[1,2]}]}",
        flattendMap.toString());
    assertEquals("{\"a\":[1,2,3],\"b\":[{\"c\":{\"d\":[1,2]}}]}",
        JsonUnflattener.unflatten(flattendMap));
  }

  @Test
  public void testUnflattenWithKeyContainsDotAndSquareBracket()
      throws JsonMappingException, JsonProcessingException {
    assertEquals("[1,[2,3],4,{\"ab.c.[\":5}]", JsonUnflattener.unflatten(
        "{\"[1][0]\":2,\"[ 0 ]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][ \\\"ab.c.[\\\" ]\":5}"));

    // Map
    assertEquals("[1,[2,3],4,{\"ab.c.[\":5}]", JsonUnflattener.unflatten(toMap(
        "{\"[1][0]\":2,\"[ 0 ]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][ \\\"ab.c.[\\\" ]\":5}")));
  }

  @Test
  public void testUnflattenWithReversedIndexesWithinObjects()
      throws IOException {
    URL url = Resources.getResource("test3.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals("{\"List\":[{\"type\":\"A\"},null,{\"type\":\"B\"}]}",
        JsonUnflattener.unflatten(json));

    // Map
    assertEquals("{\"List\":[{\"type\":\"A\"},null,{\"type\":\"B\"}]}",
        JsonUnflattener.unflatten(toMap(json)));
  }

  @Test
  public void testUnflattenWithReversedIndexes()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"[1][1]\":\"B\",\"[0][0]\":\"A\"}";

    assertEquals("[[\"A\"],[null,\"B\"]]", JsonUnflattener.unflatten(json));

    // Map
    assertEquals("[[\"A\"],[null,\"B\"]]",
        JsonUnflattener.unflatten(toMap(json)));
  }

  @Test
  public void testUnflattenWithInitComplexKey()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"[\\\"b.b\\\"].aaa\":123}";

    assertEquals("{\"b.b\":{\"aaa\":123}}", JsonUnflattener.unflatten(json));

    // Map
    assertEquals("{\"b.b\":{\"aaa\":123}}",
        JsonUnflattener.unflatten(toMap(json)));
  }

  @Test
  public void testHashCode() throws IOException {
    String json1 = "[[123]]";
    String json2 = "[[[123]]]";

    JsonUnflattener unflattener = new JsonUnflattener(json1);
    assertEquals(unflattener.hashCode(), unflattener.hashCode());
    assertEquals(unflattener.hashCode(), new JsonUnflattener(json1).hashCode());
    assertNotEquals(unflattener.hashCode(),
        new JsonUnflattener(json2).hashCode());
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEquals() throws IOException {
    String json1 = "[[123]]";
    String json2 = "[[[123]]]";

    JsonUnflattener unflattener = new JsonUnflattener(json1);
    assertTrue(unflattener.equals(unflattener));
    assertTrue(unflattener.equals(new JsonUnflattener(json1)));
    assertFalse(unflattener.equals(new JsonUnflattener(json2)));
    assertFalse(unflattener.equals(123L));
  }

  @Test
  public void testToString() throws IOException {
    String json = "[[123]]";

    assertEquals("JsonUnflattener{root=[[123]]}",
        new JsonUnflattener(json).toString());
  }

  @Test
  public void testWithKeepArrays() throws IOException {
    URL url = Resources.getResource("test4.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals(json, JsonUnflattener.unflatten(new JsonFlattener(json)
        .withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten()));

    // Map
    assertEquals(json, JsonUnflattener.unflatten(toMap(new JsonFlattener(json)
        .withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten())));
  }

  @Test
  public void testWithSeparater()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"abc\":{\"def\":123}}";
    assertEquals(json,
        new JsonUnflattener(
            new JsonFlattener(json).withSeparator('*').flatten())
                .withSeparator('*').unflatten());

    // Map
    assertEquals(json,
        new JsonUnflattener(
            toMap(new JsonFlattener(json).withSeparator('*').flatten()))
                .withSeparator('*').unflatten());
  }

  @Test
  public void testWithSeparaterException()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"abc\":{\"def\":123}}";
    try {
      new JsonUnflattener(json).withSeparator('"');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator contains illegal character(\")", e.getMessage());
    }
    try {
      new JsonUnflattener(json).withSeparator(' ');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator contains illegal character( )", e.getMessage());
    }
    try {
      new JsonUnflattener(json).withSeparator('[');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator([) is already used in brackets", e.getMessage());
    }
    try {
      new JsonUnflattener(json).withSeparator(']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator(]) is already used in brackets", e.getMessage());
    }

    // Map
    try {
      new JsonUnflattener(toMap(json)).withSeparator('"');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator contains illegal character(\")", e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withSeparator(' ');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator contains illegal character( )", e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withSeparator('[');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator([) is already used in brackets", e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withSeparator(']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Separator(]) is already used in brackets", e.getMessage());
    }
  }

  @Test
  public void testWithLeftAndRightBrackets()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"abc[\\\"A.\\\"][0]\":123}";
    assertEquals("{\"abc\":{\"A.\":[123]}}", new JsonUnflattener(json)
        .withLeftAndRightBrackets('[', ']').unflatten());

    json = "{\"abc{\\\"A.\\\"}{0}\":123}";
    assertEquals("{\"abc\":{\"A.\":[123]}}", new JsonUnflattener(json)
        .withLeftAndRightBrackets('{', '}').unflatten());

    // Map
    json = "{\"abc[\\\"A.\\\"][0]\":123}";
    assertEquals("{\"abc\":{\"A.\":[123]}}", new JsonUnflattener(toMap(json))
        .withLeftAndRightBrackets('[', ']').unflatten());

    json = "{\"abc{\\\"A.\\\"}{0}\":123}";
    assertEquals("{\"abc\":{\"A.\":[123]}}", new JsonUnflattener(toMap(json))
        .withLeftAndRightBrackets('{', '}').unflatten());
  }

  @Test
  public void testWithLeftAndRightBracketsException()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"abc\":{\"def\":123}}";
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets('#', '#');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Both brackets cannot be the same", e.getMessage());
    }
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets('"', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character(\")",
          e.getMessage());
    }
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets(' ', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character( )",
          e.getMessage());
    }
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets('.', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character(.)",
          e.getMessage());
    }
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets('[', '"');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character(\")",
          e.getMessage());
    }
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets('[', ' ');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character( )",
          e.getMessage());
    }
    try {
      new JsonUnflattener(json).withLeftAndRightBrackets('[', '.');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character(.)",
          e.getMessage());
    }

    // Map
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets('#', '#');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Both brackets cannot be the same", e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets('"', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character(\")",
          e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets(' ', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character( )",
          e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets('.', ']');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Left bracket contains illegal character(.)",
          e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets('[', '"');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character(\")",
          e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets('[', ' ');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character( )",
          e.getMessage());
    }
    try {
      new JsonUnflattener(toMap(json)).withLeftAndRightBrackets('[', '.');
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Right bracket contains illegal character(.)",
          e.getMessage());
    }
  }

  @Test
  public void testWithNonObject()
      throws JsonMappingException, JsonProcessingException {
    assertEquals("123", JsonUnflattener.unflatten("123"));
    assertEquals("\"abc\"", JsonUnflattener.unflatten("\"abc\""));
    assertEquals("true", JsonUnflattener.unflatten("true"));
    assertEquals("[1,2,3]", JsonUnflattener.unflatten("[1,2,3]"));
  }

  @Test
  public void testWithNestedArrays() {
    assertEquals("[[{\"abc\":{\"def\":123}}]]",
        JsonUnflattener.unflatten("[[{\"abc.def\":123}]]"));
  }

  @Test
  public void testPrintMode() throws IOException {
    String src = "{\"abc.def\":123}";
    String json =
        new JsonUnflattener(src).withPrintMode(PrintMode.MINIMAL).unflatten();
    assertEquals(mapper.readTree(json).toString(), json);

    json = new JsonUnflattener(src).withPrintMode(PrintMode.PRETTY).unflatten();
    assertEquals(mapper.readTree(json).toPrettyString(), json);
  }

  @Test
  public void testNoCache()
      throws JsonMappingException, JsonProcessingException {
    JsonUnflattener ju = new JsonUnflattener("{\"abc.def\":123}");
    assertNotSame(ju.unflatten(), ju.unflatten());
    assertEquals("{\"abc\":{\"def\":123}}",
        ju.withPrintMode(PrintMode.MINIMAL).unflatten());

    // Map
    ju = new JsonUnflattener(toMap("{\"abc.def\":123}"));
    assertNotSame(ju.unflatten(), ju.unflatten());
    assertEquals("{\"abc\":{\"def\":123}}",
        ju.withPrintMode(PrintMode.MINIMAL).unflatten());
  }

  @Test
  public void testNullPointerException() {
    assertThrows(NullPointerException.class, () -> {
      new JsonUnflattener("{\"abc.def\":123}").withPrintMode(null);
    });
  }

  @Test
  public void testInitByReader() throws IOException {
    StringReader sr = new StringReader("{\"abc.def\":123}");

    assertEquals(new JsonUnflattener("{\"abc.def\":123}"),
        new JsonUnflattener(sr));
  }

  @Test
  public void testInitByMap()
      throws JsonMappingException, JsonProcessingException {
    assertEquals(new JsonUnflattener("{\"abc.def\":123}"),
        new JsonUnflattener(toMap("{\"abc.def\":123}")));
  }

  @Test
  public void testFlattenModeMongodb() throws IOException {
    URL url = Resources.getResource("test_mongo.json");
    String expectedJson = Resources.toString(url, Charsets.UTF_8);

    URL urlMongo = Resources.getResource("test_mongo_flattened.json");
    String json = Resources.toString(urlMongo, Charsets.UTF_8);

    JsonUnflattener ju =
        new JsonUnflattener(json).withFlattenMode(FlattenMode.MONGODB);
    assertEquals(mapper.readTree(expectedJson).toString(), ju.unflatten());

    // Map
    ju = new JsonUnflattener(toMap(json)).withFlattenMode(FlattenMode.MONGODB);
    assertEquals(mapper.readTree(expectedJson).toString(), ju.unflatten());
  }

  @Test
  public void testWithKeyTransformer()
      throws JsonMappingException, JsonProcessingException {
    String json = "{\"abc.de_f\":123}";
    JsonUnflattener ju =
        new JsonUnflattener(json).withFlattenMode(FlattenMode.MONGODB)
            .withKeyTransformer(new KeyTransformer() {

              @Override
              public String transform(String key) {
                return key.replace('_', '.');
              }

            });
    assertEquals("{\"abc\":{\"de.f\":123}}", ju.unflatten());

    // Map
    ju = new JsonUnflattener(toMap(json)).withFlattenMode(FlattenMode.MONGODB)
        .withKeyTransformer(new KeyTransformer() {

          @Override
          public String transform(String key) {
            return key.replace('_', '.');
          }

        });
    assertEquals("{\"abc\":{\"de.f\":123}}", ju.unflatten());
  }

  @Test
  public void testWithFlattenModeKeepBottomArrays() throws IOException {
    URL url = Resources.getResource("test_keep_primitive_arrays.json");
    String expectedJson = Resources.toString(url, Charsets.UTF_8);

    URL urlKBA =
        Resources.getResource("test_keep_primitive_arrays_flattened.json");
    String json = Resources.toString(urlKBA, Charsets.UTF_8);

    JsonUnflattener ju = new JsonUnflattener(json)
        .withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS);
    assertEquals(mapper.readTree(expectedJson).toString(), ju.unflatten());

    // Map
    ju = new JsonUnflattener(toMap(json))
        .withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS);
    assertEquals(mapper.readTree(expectedJson).toString(), ju.unflatten());
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

    JsonUnflattener ju = new JsonUnflattener(new JacksonJsonCore(mapper), json);
    ju.withPrintMode(PrintMode.PRETTY);
    assertEquals(mapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(mapper.readTree(json)), ju.unflatten());

    ju = new JsonUnflattener(new JacksonJsonCore(mapper),
        new StringReader(json));
    ju.withPrintMode(PrintMode.PRETTY);
    assertEquals(mapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(mapper.readTree(json)), ju.unflatten());

    ju = new JsonUnflattener(new JacksonJsonCore(mapper),
        mapper.readValue(json, new TypeReference<Map<String, Object>>() {}));
    ju.withPrintMode(PrintMode.PRETTY);
    assertEquals(mapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(mapper.readTree(json)), ju.unflatten());
  }

}
