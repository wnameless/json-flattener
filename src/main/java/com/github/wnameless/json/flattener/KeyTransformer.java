/*
 *
 * Copyright 2017 Wei-Ming Wu
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

import com.github.wnameless.json.unflattener.JsonUnflattener;

/**
 * 
 * {@link KeyTransformer} defines an interface to transform keys in
 * {@link JsonFlattener} or {@link JsonUnflattener}.
 * 
 * @author Wei-Ming Wu
 *
 */
public interface KeyTransformer {

  /**
   * Transforms the given key by this function.
   * 
   * @param key
   *          any JSON key
   * @return new JSON key
   */
  String transform(String key);

}
