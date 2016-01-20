/**
 *
 * @author Wei-Ming Wu
 *
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

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JsonFlattenerTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorException() {
    new JsonFlattener("123");
  }

  @Test
  public void testFlattenAsMap() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals("{a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}",
        JsonFlattener.flattenAsMap(json).toString());
  }

  @Test
  public void testFlatten() throws IOException {
    URL url = Resources.getResource("test2.json");
    String json = Resources.toString(url, Charsets.UTF_8);

    assertEquals(
        "{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}",
        JsonFlattener.flatten(json));

    assertEquals("{\"[0].a\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}",
        JsonFlattener.flatten("[{\"a\":1},2,{\"c\":[3,4]}]"));
  }

  @Test
  public void testFlattenWithKeyContainsDotAndSquareBracket()
      throws IOException {
    assertEquals(
        "{\"[0][\\\"a.a.[\\\"]\":1,\"[1]\":2,\"[2].c[0]\":3,\"[2].c[1]\":4}",
        JsonFlattener.flatten("[{\"a.a.[\":1},2,{\"c\":[3,4]}]"));
  }

  @Test
  public void testHashCode() throws IOException {
    URL url1 = Resources.getResource("test.json");
    String json1 = Resources.toString(url1, Charsets.UTF_8);
    URL url2 = Resources.getResource("test2.json");
    String json2 = Resources.toString(url2, Charsets.UTF_8);

    JsonFlattener flattener = new JsonFlattener(json1);
    assertTrue(flattener.hashCode() == flattener.hashCode());
    assertTrue(flattener.hashCode() == new JsonFlattener(json1).hashCode());
    assertFalse(flattener.hashCode() == new JsonFlattener(json2).hashCode());
  }

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
  public void testWithEmptyJsonObjectå() throws IOException {
    String json = "{}";
    assertEquals("{}", new JsonFlattener(json).flatten());
    assertEquals(newHashMap(), new JsonFlattener(json).flattenAsMap());
    assertEquals(json,
        JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
  }

  @Test
  public void testWithEmptyJsonArray() throws IOException {
    String json = "[]";
    assertEquals("[]", new JsonFlattener(json).flatten());
    assertEquals(newHashMap(), new JsonFlattener(json).flattenAsMap());
  }

  @Test
  public void testWithEmptyArray() {
    String json = "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[]}";
    assertEquals("{\"no\":\"1\",\"name\":\"riya\",\"marks\":[]}",
        new JsonFlattener(json).flatten());
    assertEquals(json,
        JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
  }

  @Test
  public void testWithEmptyObject() {
    String json = "{\"no\":\"1\",\"name\":\"riya\",\"marks\":[{}]}";
    assertEquals("{\"no\":\"1\",\"name\":\"riya\",\"marks[0]\":{}}",
        new JsonFlattener(json).flatten());
    assertEquals(json,
        JsonUnflattener.unflatten(new JsonFlattener(json).flatten()));
  }

}
