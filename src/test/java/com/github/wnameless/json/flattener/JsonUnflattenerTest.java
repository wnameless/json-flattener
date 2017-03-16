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
package com.github.wnameless.json.flattener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.Test;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.PrettyPrint;
import com.eclipsesource.json.WriterConfig;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JsonUnflattenerTest {

  @Test
  public void testUnflatten() {
    assertEquals(
        "{\"a\":{\"b\":1,\"c\":null,\"d\":[false,true,{\"sss\":777,\"vvv\":888}]},\"e\":\"f\",\"g\":2.3}",
        JsonUnflattener.unflatten(
            "{\"a.b\":1,\"a.c\":null,\"a.d[1]\":true,\"a.d[0]\":false,\"a.d[2].sss\":777,\"a.d[2].vvv\":888,\"e\":\"f\",\"g\":2.3}"));

    assertEquals("[1,[2,3],4,{\"abc\":5}]", JsonUnflattener.unflatten(
        "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3].abc\":5}"));
  }

  @Test
  public void testUnflattenWithKeyContainsDotAndSquareBracket() {
    assertEquals("[1,[2,3],4,{\"ab.c.[\":5}]", JsonUnflattener.unflatten(
        "{\"[1][0]\":2,\"[ 0 ]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][ \\\"ab.c.[\\\" ]\":5}"));
  }

  @Test
  public void testUnflattenWithReversedIndexesWithinObjects()
      throws IOException {
    URL url = Resources.getResource("test3.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals("{\"List\":[{\"type\":\"A\"},null,{\"type\":\"B\"}]}",
        JsonUnflattener.unflatten(json));
  }

  @Test
  public void testUnflattenWithReversedIndexes() {
    String json = "{\"[1][1]\":\"B\",\"[0][0]\":\"A\"}";

    assertEquals("[[\"A\"],[null,\"B\"]]", JsonUnflattener.unflatten(json));
  }

  @Test
  public void testUnflattenWithInitComplexKey() {
    String json = "{\"[\\\"b.b\\\"].aaa\":123}";

    assertEquals("{\"b.b\":{\"aaa\":123}}", JsonUnflattener.unflatten(json));
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
  }

  @Test
  public void testWithSeparater() {
    String json = "{\"abc\":{\"def\":123}}";
    assertEquals(json,
        new JsonUnflattener(
            new JsonFlattener(json).withSeparator('*').flatten())
                .withSeparator('*').unflatten());
  }

  @Test
  public void testWithNonObject() {
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
    StringWriter sw = new StringWriter();
    Json.parse(json).writeTo(sw, WriterConfig.MINIMAL);
    assertEquals(sw.toString(), json);

    json =
        new JsonUnflattener(src).withPrintMode(PrintMode.REGULAR).unflatten();
    sw = new StringWriter();
    Json.parse(json).writeTo(sw, PrettyPrint.singleLine());
    assertEquals(sw.toString(), json);

    json = new JsonUnflattener(src).withPrintMode(PrintMode.PRETTY).unflatten();
    sw = new StringWriter();
    Json.parse(json).writeTo(sw, WriterConfig.PRETTY_PRINT);
    assertEquals(sw.toString(), json);
  }

  @Test
  public void testNoCache() {
    JsonUnflattener ju = new JsonUnflattener("{\"abc.def\":123}");
    assertNotSame(ju.unflatten(), ju.unflatten());
    assertEquals("{\"abc\": {\"def\": 123}}",
        ju.withPrintMode(PrintMode.REGULAR).unflatten());
  }

  @Test(expected = NullPointerException.class)
  public void testNullPointerException() {
    new JsonUnflattener("{\"abc.def\":123}").withPrintMode(null);
  }

  @Test
  public void testLazy() throws IOException {
    URL url = Resources.getResource("test.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    long t = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      new JsonUnflattener(json);
    }
    long normalTime = System.currentTimeMillis() - t;

    t = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
      JsonUnflattener.lazy(json);
    }
    long lazyTime = System.currentTimeMillis() - t;

    assertTrue(normalTime > lazyTime);
  }

  @Test
  public void testInitByReader() throws IOException {
    StringReader sr = new StringReader("{\"abc.def\":123}");

    assertEquals(new JsonUnflattener(sr),
        new JsonUnflattener("{\"abc.def\":123}"));
    sr.reset();
    assertEquals(JsonUnflattener.lazy(sr),
        new JsonUnflattener("{\"abc.def\":123}"));
  }

}
