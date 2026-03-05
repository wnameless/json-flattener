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

import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.flattener.JsonFlattenerFactory;
import com.github.wnameless.json.base.GsonJsonCore;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.github.wnameless.json.unflattener.JsonUnflattenerFactory;

import java.util.function.Consumer;

/**
 * Round-trip fuzzer: flatten → unflatten → flatten.
 *
 * Verifies that if a JSON object can be flattened and unflattened, the
 * second flatten of the restored object produces a structurally equivalent
 * result (same number of keys). Any key-count mismatch is surfaced as an
 * AssertionError so OSS-Fuzz captures it as a reproducible finding.
 */
public class RoundTripFuzzer {

    static FlattenMode[] flattenModes = {
        FlattenMode.NORMAL,
        FlattenMode.KEEP_ARRAYS,
        FlattenMode.KEEP_PRIMITIVE_ARRAYS,
        FlattenMode.MONGODB
    };

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        FlattenMode mode = data.pickValue(flattenModes);
        char separator = data.consumeChar();
        String json = data.consumeRemainingAsString();

        // Separators must be a safe, predictable non-alphanumeric char
        if (Character.isLetterOrDigit(separator) || separator == '"' || separator == '\\') {
            return;
        }

        try {
            // Step 1: flatten
            Consumer<JsonFlattener> flatCfg = jf -> jf.withFlattenMode(mode).withSeparator(separator);
            JsonFlattenerFactory flatFactory = new JsonFlattenerFactory(flatCfg, new GsonJsonCore());
            String flat1 = flatFactory.build(json).flatten();

            // Step 2: unflatten back
            Consumer<JsonUnflattener> unflatCfg = ju -> ju.withFlattenMode(mode).withSeparator(separator);
            JsonUnflattenerFactory unflatFactory = new JsonUnflattenerFactory(unflatCfg, new GsonJsonCore());
            String restored = unflatFactory.build(flat1).unflatten();

            // Step 3: flatten again — must not throw and must be structurally equivalent
            String flat2 = flatFactory.build(restored).flatten();

            // Consistency assertion: key count must be identical
            long keys1 = flat1.chars().filter(c -> c == ':').count();
            long keys2 = flat2.chars().filter(c -> c == ':').count();
            if (keys1 != keys2) {
                throw new AssertionError(String.format(
                    "Round-trip key count mismatch (mode=%s sep='%c'): %d vs %d%ninput: %s%nflat1: %s%nrestored: %s%nflat2: %s",
                    mode, separator, keys1, keys2, json, flat1, restored, flat2));
            }

        } catch (AssertionError e) {
            throw e; // surface to OSS-Fuzz as a crash
        } catch (RuntimeException ignored) {
            // invalid JSON or unsupported separator — expected, ignore
        }
    }
}
