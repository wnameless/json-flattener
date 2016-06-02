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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.junit.Test;

import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class JsonUnflattenerTest {

  @Test
  public void testPrivateContructor() throws Exception {
    Constructor<JsonUnflattener> c =
        JsonUnflattener.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(c.getModifiers()));
    c.setAccessible(true);
    c.newInstance();
  }

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
  public void testUnflattenKeepArray() {
    String json = "{\"a.b\":1,\"a.c\":null,\"a.d\":[false,{\"i.j\":[false,true]}],\"e\":\"f\",\"g\":2.3,\"z\":[]}";
    assertEquals("{\"a\":{\"b\":1,\"c\":null,\"d\":[false,{\"i\":{\"j\":[false,true]}}]},\"e\":\"f\",\"g\":2.3,\"z\":[]}", JsonUnflattener.unflatten(json));
  }


  @Test
  public void testUnflattenKeepArrayNested() {
    String json = "[[{\"a.b\":3}]]";
    assertEquals("[[{\"a\":{\"b\":3}}]]", JsonUnflattener.unflatten(json));
  }

  @Test
  public void testUnflattenKeepArrayEmpty() {
    String json = "[{}]";
    assertEquals("[{}]", JsonUnflattener.unflatten(json));
  }

}
