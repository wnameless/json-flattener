/*
 *
 * Copyright 2016 Wei-Ming Wu
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

import java.util.HashMap;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

/**
 * 
 * {@link StringEscapePolicy} lists all acceptable JSON string escape policy of
 * the {@link JsonFlattener}.
 * 
 * @author Wei-Ming Wu
 *
 */
public enum StringEscapePolicy {

  /**
   * Escapes JSON special characters.
   */
  NORMAL(new AggregateTranslator(new LookupTranslator(new HashMap<CharSequence, CharSequence>() {
    private static final long serialVersionUID = 1L;
    {
      put("\"", "\\\"");
      put("\\", "\\\\");
      put("/", "\\/");
    }
  }), new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE))),

  /**
   * Escapes JSON special characters and Unicode characters.
   */
  ALL_UNICODES(StringEscapeUtils.ESCAPE_JSON);

  private final CharSequenceTranslator translator;

  private StringEscapePolicy(CharSequenceTranslator translator) {
    this.translator = translator;
  }

  public CharSequenceTranslator getCharSequenceTranslator() {
    return translator;
  }

}
