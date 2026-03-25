// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.example;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import com.github.wnameless.json.flattener.*;
import com.github.wnameless.json.base.GsonJsonCore;
import com.github.wnameless.json.base.JsonCore;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.github.wnameless.json.unflattener.JsonUnflattenerFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AdvancedFuzzer {
    static StringEscapePolicy[] stringEscapePolicies = {
        StringEscapePolicy.DEFAULT,
        StringEscapePolicy.ALL,
        StringEscapePolicy.ALL_BUT_SLASH,
        StringEscapePolicy.ALL_BUT_UNICODE,
        StringEscapePolicy.ALL_BUT_SLASH_AND_UNICODE
    };

    static FlattenMode[] flattenModes = {
        FlattenMode.NORMAL,
        FlattenMode.MONGODB,
        FlattenMode.KEEP_ARRAYS,
        FlattenMode.KEEP_PRIMITIVE_ARRAYS
    };

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        try {
            int testCase = data.consumeInt(0, 5);
            
            switch (testCase) {
                case 0:
                    testKeyTransformer(data);
                    break;
                case 1:
                    testCustomBrackets(data);
                    break;
                case 2:
                    testReaderConstructors(data);
                    break;
                case 3:
                    testIgnoreReservedCharacters(data);
                    break;
                case 4:
                    testStaticMethods(data);
                    break;
                case 5:
                    testMapBasedUnflatten(data);
                    break;
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static void testKeyTransformer(FuzzedDataProvider data) {
        FlattenMode mode = data.pickValue(flattenModes);
        KeyTransformer upperCaseTransformer = key -> key.toUpperCase();
        KeyTransformer prefixTransformer = key -> "prefix_" + key;

        JsonFlattener flattener = new JsonFlattener(new GsonJsonCore(), data.consumeRemainingAsString())
            .withFlattenMode(mode)
            .withKeyTransformer(upperCaseTransformer);
        flattener.flatten();
        flattener.flattenAsMap();

        JsonUnflattener unflattener = new JsonUnflattener(new GsonJsonCore(), data.consumeRemainingAsString())
            .withFlattenMode(mode)
            .withKeyTransformer(prefixTransformer);
        unflattener.unflatten();
        unflattener.unflattenAsMap();
    }

    private static void testCustomBrackets(FuzzedDataProvider data) {
        char leftBracket = data.consumeChar();
        char rightBracket = data.consumeChar();
        
        if (leftBracket == rightBracket) return;
        if (leftBracket == '"' || rightBracket == '"') return;
        if (Character.isLetterOrDigit(leftBracket) || Character.isLetterOrDigit(rightBracket)) return;
        
        FlattenMode mode = data.pickValue(flattenModes);
        String json = data.consumeRemainingAsString();

        try {
            JsonFlattener flattener = new JsonFlattener(json)
                .withFlattenMode(mode)
                .withLeftAndRightBrackets(leftBracket, rightBracket);
            flattener.flatten();
            flattener.flattenAsMap();

            String flat = flattener.flatten();
            JsonUnflattener unflattener = new JsonUnflattener(flat)
                .withFlattenMode(mode)
                .withLeftAndRightBrackets(leftBracket, rightBracket);
            unflattener.unflatten();
            unflattener.unflattenAsMap();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void testReaderConstructors(FuzzedDataProvider data) {
        FlattenMode mode = data.pickValue(flattenModes);
        StringEscapePolicy sep = data.pickValue(stringEscapePolicies);
        String json = data.consumeRemainingAsString();

        try {
            JsonFlattener flattener = new JsonFlattener(new GsonJsonCore(), new StringReader(json))
                .withFlattenMode(mode)
                .withStringEscapePolicy(sep);
            flattener.flatten();
            flattener.flattenAsMap();

            String flat = flattener.flatten();
            JsonUnflattener unflattener = new JsonUnflattener(new GsonJsonCore(), new StringReader(flat))
                .withFlattenMode(mode);
            unflattener.unflatten();
            unflattener.unflattenAsMap();
        } catch (Exception ignored) {
        }
    }

    private static void testIgnoreReservedCharacters(FuzzedDataProvider data) {
        FlattenMode mode = data.pickValue(flattenModes);
        StringEscapePolicy sep = data.pickValue(stringEscapePolicies);
        
        JsonFlattener flattener = new JsonFlattener(new GsonJsonCore(), data.consumeRemainingAsString())
            .withFlattenMode(mode)
            .withStringEscapePolicy(sep)
            .ignoreReservedCharacters();
        flattener.flatten();
        flattener.flattenAsMap();

        String flat = flattener.flatten();
        JsonUnflattener unflattener = new JsonUnflattener(new GsonJsonCore(), flat)
            .withFlattenMode(mode);
        unflattener.unflatten();
        unflattener.unflattenAsMap();
    }

    private static void testStaticMethods(FuzzedDataProvider data) {
        String json = data.consumeRemainingAsString();

        try {
            JsonFlattener.flatten(json);
            JsonFlattener.flattenAsMap(json);
        } catch (Exception ignored) {
        }

        try {
            String flat = JsonFlattener.flatten(json);
            JsonUnflattener.unflatten(flat);
            JsonUnflattener.unflattenAsMap(flat);
        } catch (Exception ignored) {
        }
    }

    private static void testMapBasedUnflatten(FuzzedDataProvider data) {
        FlattenMode mode = data.pickValue(flattenModes);
        String json = data.consumeRemainingAsString();

        try {
            JsonFlattener flattener = new JsonFlattener(json).withFlattenMode(mode);
            Map<String, Object> flatMap = flattener.flattenAsMap();

            JsonUnflattener.unflatten(flatMap);
            JsonUnflattener.unflattenAsMap(flatMap);

            Consumer<JsonUnflattener> cfg = ju -> ju.withFlattenMode(mode);
            JsonUnflattenerFactory factory = new JsonUnflattenerFactory(cfg, new GsonJsonCore());
            factory.build(flatMap).unflatten();
            factory.build(flatMap).unflattenAsMap();
        } catch (Exception ignored) {
        }
    }
}
