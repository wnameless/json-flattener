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

import java.util.Map.Entry;

public interface JsonObjectBase<JV extends JsonValueBase>
    extends Iterable<Entry<String, JV>> {

  public JsonValueBase get(String name);

  public int asInt();

  public long asLong();

  public float asFloat();

  public double asDouble();

  public boolean asBoolean();

  public String asString();

}
