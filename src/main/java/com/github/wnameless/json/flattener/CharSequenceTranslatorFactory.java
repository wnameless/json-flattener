/*
 *
 * Copyright 2018 Wei-Ming Wu
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

import org.apache.commons.text.translate.CharSequenceTranslator;

/**
 * 
 * {@link CharSequenceTranslatorFactory} is designed to enhance the
 * {@link StringEscapePolicy}.<br>
 * Any method which accepts a {@link StringEscapePolicy} (eg:
 * {@link JsonFlattener#withStringEscapePolicy(CharSequenceTranslatorFactory)
 * JsonFlattener#withStringEscapePolicy}) now accepts
 * {@link CharSequenceTranslatorFactory} as well.<br>
 * <br>
 * Furthermore, anyone can provide their own {@link StringEscapePolicy} by
 * implementing a {@link CharSequenceTranslatorFactory}.
 * 
 * @author Wei-Ming Wu
 * @since v0.5.0
 *
 */
public interface CharSequenceTranslatorFactory {

  /**
   * Returns a {@link CharSequenceTranslator}
   * 
   * @return {@link CharSequenceTranslator}
   */
  CharSequenceTranslator getCharSequenceTranslator();

}
