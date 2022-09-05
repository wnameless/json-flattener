[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless.json/json-flattener/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless.json/json-flattener)
[![codecov](https://codecov.io/gh/wnameless/json-flattener/branch/master/graph/badge.svg)](https://codecov.io/gh/wnameless/json-flattener)

json-flattener
=============
A Java utility is designed to FLATTEN nested JSON objects and even more to UNFLATTEN them back.

## Purpose
Converts a nested JSON
```json
{ "a":
  { "b": 1,
    "c": null,
    "d": [false, true]
  },
  "e": "f",
  "g": 2.3
}
```
into a flattened JSON
```json
{ "a.b": 1,
  "a.c": null,
  "a.d[0]": false,
  "a.d[1]": true,
  "e": "f",
  "g": 2.3
}
```
or a Java Map<br>
&nbsp;&nbsp;{a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

## Maven Repo
```xml
<dependency>
	<groupId>com.github.wnameless.json</groupId>
	<artifactId>json-flattener</artifactId>
	<version>0.14.0</version>
</dependency>
```
Since v0.5.0, Java 8 required.<br>
Since v0.6.0, StringEscapePolicy.DEFAULT, which escapes all special characters but slash('/') and Unicode, becomes the default setting.<br>
Since v0.9.0, Java Module supported.<br>
```diff
- Since v0.7.0, group ID is changed from [com.github.wnameless] to [com.github.wnameless.json].
```
```diff
+ Since v0.10.0, The Map produced by JsonFlattener#flattenAsMap after serialization is now identical to the JSON produced by JsonFlattener#flatten.
+ Before v0.10.0, the serialized flattened Map may be different at some edge cases(ex: input keys contain separator('.')).
```

## Quick Start
```java
String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";
Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

System.out.println(flattenJson);
// {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

String jsonStr = JsonFlattener.flatten(json);
System.out.println(jsonStr);
// {"a.b":1,"a.c":null,"a.d[0]":false,"a.d[1]":true,"e":"f","g":2.3}

String nestedJson = JsonUnflattener.unflatten(jsonStr);
System.out.println(nestedJson);
// {"a":{"b":1,"c":null,"d":[false,true]},"e":"f","g":2.3}

// Support JSON keys which contain dots or square brackets
String flattendJsonWithDotKey = JsonFlattener.flatten("[{\"a.a.[\":1},2,{\"c\":[3,4]}]");
System.out.println(flattendJsonWithDotKey);
// {"[0][\"a.a.[\"]":12,"[1]":2,"[2].c[0]":3,"[2].c[1]":4}

String nestedJsonWithDotKey = JsonUnflattener.unflatten(
        "{\"[1][0];\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][\\\"ab.c.[\\\"]\":5}");
System.out.println(nestedJsonWithDotKey);
// [1,[2,3],4,{"ab.c.[":5}]
```

## New Features (since v0.14.0)
### JsonFlattenerFactory - produces any JsonFlattener with preconfigured settings
```java
// Inside Spring configuration class
@Bean
public JsonFlattenerFactory jsonFlattenerFactory() {
  // Changes the default PrintMode from MINIMAL to PRETTY
  Consumer<JsonFlattener> configurer = jf -> jf.withPrintMode(PrintMode.PRETTY);
  // Alters the default JsonCore from Jackson to GSON
  JsonCore<?> jsonCore = new GsonJsonCore();

  return new JsonFlattenerFactory(configurer, jsonCore);
}

// In any other Spring environment class
@Autowired
JsonFlattenerFactory jsonFlattenerFactory;

public void usageExamples(String json) {
  JsonFlattener jf1 = jsonFlattenerFactory.build(json);
  JsonFlattener jf2 = jsonFlattenerFactory.build(new GsonJsonCore().parse(json));
  JsonFlattener jf3 = jsonFlattenerFactory.build(new StringReader(json));
}
```

## New Features (since v0.13.0)
### IgnoreReservedCharacters - reserved characters in keys can be ignored
```java
String json = "{\"matrix\":{\"agent.smith\":\"1999\"}}";

System.out.println(JsonFlattener.flatten(json));
// {"matrix[\"agent.smith\"]":"1999"}

System.out.println(new JsonFlattener(json).ignoreReservedCharacters().flatten());
// {"matrix.agent.smith":"1999"}
// The escape of reserved character('.') has been ignored

new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB).flatten();
// Throws IllegalArgumentException

System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB).ignoreReservedCharacters().flatten());
// {"matrix.agent.smith":"1999"}
// The check of reserved character('.') has been ignored
```

## New Features (since v0.12.0)
### JsonCore - customized JSON libarary supported
```java
JsonFlattener jf;
JsonUnflattener ju;

ObjectMapper mapper = new ObjectMapper() {
  {
    configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
  }
};
jf = new JsonFlattener(new JacksonJsonCore(mapper), json);
ju = new JsonUnflattener(new JacksonJsonCore(mapper), json);

Gson gson = new GsonBuilder().serializeNulls().create();
jf = new JsonFlattener(new GsonJsonCore(gson), json);
ju = new JsonUnflattener(new GsonJsonCore(gson), json);
```

## New Features (since v0.11.0)
### JsonUnflattener.unflatten(Map)
```java
String json = "{\"abc\":{\"def\":[1,2,{\"g\":{\"h\":[3]}}]}}";
Map<String, Object> flattenedMap = JsonFlattener.flattenAsMap(json);

String unflattenedJson = JsonUnflattener.unflatten(flattenedMap);
```
### JsonUnflattener.unflattenAsMap
```java
String json = "{\"abc\":{\"def\":[1,2,{\"g\":{\"h\":[3]}}]}}";
String flattenedJson = JsonFlattener.flatten(json);
Map<String, Object> flattenedMap = JsonFlattener.flattenAsMap(json);

Map<String, Object> unflattenedMap;
unflattenedMap = JsonUnflattener.unflattenAsMap(flattenedJson);
unflattenedMap = JsonUnflattener.unflattenAsMap(flattenedMap);
```

## New Features (since v0.8.1)
### JsonFlattener.flattenAsMap(JsonValueBase)
```java
JsonValueBase<?> jsonVal;

// JacksonJsonValue, which is provided by json-base lib, can wrap Jackson jsonNode to JsonValueBase
jsonVal = new JacksonJsonValue(jsonNode);

Map<String, Object> flattenMap = JsonFlattener.flattenAsMap(jsonVal);
```

## New Features (since v0.8.0)
### FlattenMode.KEEP_PRIMITIVE_ARRAYS
```java
String json = "{\"ary\":[true,[1, 2, 3],false]}";
System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS).flatten());
// {"ary[0]":true,"ary[1]":[1,2,3],"ary[2]":false}
```
This mode only keeps arrays which contain only primitive types(strings, numbers, booleans, and null).

## New Features (since v0.7.0)
### JsonValueBase, which comes from json-base lib, is introduced to improve performance
```java
JsonValueBase<?> jsonVal;

// GsonJsonValue, which is provided by json-base lib, can wrap Gson jsonElement to JsonValueBase
jsonVal = new GsonJsonValue(jsonElement);

// JacksonJsonValue, which is provided by json-base lib, can wrap Jackson jsonNode to JsonValueBase
jsonVal = new JacksonJsonValue(jsonNode);

// You can also implement the JsonValueBase interface for any JSON lib you are using
jsonVal = new CostumeJsonValue(yourJsonVal);

new JsonFlattener(jsonVal);
```

## New Features (since v0.6.0)
### More options in StringEscapePolicy enum
```java
StringEscapePolicy.ALL //                       Escapes all JSON special characters and Unicode
StringEscapePolicy.ALL_BUT_SLASH //             Escapes all JSON special characters and Unicode but slash('/')
StringEscapePolicy.ALL_BUT_UNICODE //           Escapes all JSON special characters but Unicode
StringEscapePolicy.ALL_BUT_SLASH_AND_UNICODE // Escapes all JSON special characters but slash('/') and Unicode
StringEscapePolicy.DEFAULT //                   Escapes all JSON special characters but slash('/') and Unicode
```

## New Features (since v0.5.0)
### CharSequenceTranslatorFactory
```java
public class MyStringEscapePolicy implements CharSequenceTranslatorFactory { ... }
```
StringEscapePolicy can be customized by implementing the CharSequenceTranslatorFactory interface.

For example, if you don't want the slash(/) and backslash(\\) to be escaped:
```java
new JsonFlattener(YOUR_JSON)

        .withStringEscapePolicy(new CharSequenceTranslatorFactory() {

          @Override
          public CharSequenceTranslator getCharSequenceTranslator() {
            return new AggregateTranslator(
                new LookupTranslator(new HashMap<CharSequence, CharSequence>() {
                  private static final long serialVersionUID = 1L;
                  {
                    put("\"", "\\\"");
                    // put("\\", "\\\\");
                    // put("/", "\\/"); 
                  }
                }), new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE));
          }

        });
```

## New Features (since v0.4.0)
### FlattenMode.MONGODB (dot notation)
```java
String json = "{\"abc\":{\"def\":[123]}}";
System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB).flatten());
// {"abc.def.0":123}

json = "{\"abc.def.0\":123}";
System.out.println(new JsonUnflattener(json).withFlattenMode(FlattenMode.MONGODB).unflatten());
// {"abc":{"def":[123]}}

// With FlattenMode.MONGODB, separator can still be changed
json = "{\"abc\":{\"def\":[123]}}";
System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB).withSeparator('*').flatten());
// {"abc*def*0":123}

json = "{\"abc*def*0\":123}";
System.out.println(new JsonUnflattener(json).withFlattenMode(FlattenMode.MONGODB).withSeparator('*').unflatten());
// {"abc":{"def":[123]}}
```

### KeyTransformer
```java
String json = "{\"abc\":{\"de.f\":123}}";
JsonFlattener jf = new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB);

// This will throw an exception because FlattenMode.MONGODB won't support separator(.) in the key
jf.flatten();

// KeyTransformer can be used to manipulate keys before flattening 
KeyTransformer kt = new KeyTransformer() {
  @Override
  public String transform(String key) {
    return key.replace('.', '_');
  }
};
jf.withKeyTransformer(kt);
System.out.println(jf.flatten());
// {"abc.de_f":123}

// KeyTransformer should be set in JsonUnflattener as well
json = "{\"abc.de_f\":123}";
kt = new KeyTransformer() {
  @Override
  public String transform(String key) {
    return key.replace('_', '.');
  }
};
JsonUnflattener ju = new JsonFlattener(json).withFlattenMode(FlattenMode.MONGODB).withKeyTransformer(kt);
System.out.println(ju.unflatten());
// {"abc":{"de.f":123}}
```

## New Features (since v0.3.0)
### LeftAndRightBrackets
```java
// JsonFlattener - Brackets can be changed from square brackets([]) to any 2 arbitrary characters
String json = "{\"abc\":{\"def\":[123]}}";
System.out.println(new JsonFlattener(json).withLeftAndRightBrackets('(', ')').flatten());
// {"abc.def(0)":123}

// JsonUnflattener - if special brackets are using, it should be set into the unflattener as well
json = "{"abc.def(0)":123}";
System.out.println(new JsonUnflattener(json).withLeftAndRightBrackets('(', ')').unflatten());
// {"abc":{"def":[123]}}
```

### Reader
```java
InputStream inputStream = new FileInputStream("simple.json");
Reader reader = new InputStreamReader(inputStream);

// Support Reader as input 
JsonFlattener jf = new JsonFlattener(reader);
JsonUnflattener ju = new JsonUnflattener(reader);
```

## New Features (since v0.2.0)
### FlattenMode
```java
String json = "{\"abc\":{\"def\":[1,2,{\"g\":{\"h\":[3]}}]}}";

// FlattenMode.NORMAL(default) - flatten everything
System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.NORMAL).flatten());
// {"abc.def[0]":1,"abc.def[1]":2,"abc.def[2].g.h[0]":3}

// FlattenMode.KEEP_ARRAYS - flatten all except arrays
System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_ARRAYS).flatten());
// {"abc.def":[1,2,{"g.h":[3]}]}

// When the flattened outcome can NOT suit in a Java Map, it will still be put in the Map with "root" as its key. 
Map<String, Object> map = new JsonFlattener("[[123]]").withFlattenMode(FlattenMode.KEEP_ARRAYS).flattenAsMap();
System.out.println(map.get("root"));
// [[123]]
```
### StringEscapePolicy
```java
String json = "{\"abc\":{\"def\":\"太極\\t\"}}";

// StringEscapePolicy.NORMAL(default) - escape only speacial characters
System.out.println(new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.NORMAL).flatten());
// {"abc.def":"太極\t"}

// StringEscapePolicy.ALL_UNICODES - escape speacial characters and unicodes
System.out.println(new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.ALL_UNICODES).flatten());
// {"abc.def":"\u592A\u6975\t"}
```
### Separator
```java
// JsonFlattener - separator can be changed from dot(.) to an arbitrary character
String json = "{\"abc\":{\"def\":123}}";
System.out.println(new JsonFlattener(json).withSeparator('*').flatten());
// {"abc*def":123}

// JsonUnflattener - if a special separator is using, it should be set into the unflattener as well
json = "{\"abc*def\":123}";
System.out.println(new JsonUnflattener(json).withSeparator('*').unflatten());
// {"abc":{"def":123}}
```
### PrintMode
```java
String json = "{\"abc\":{\"def\":123}}";

// JsonFlattener
// PrintMode.MINIMAL(default)
System.out.println(new JsonFlattener(json).withPrintMode(PrintMode.MINIMAL).flatten());
// {"abc.def":123}

// PrintMode.PRETTY
System.out.println(new JsonFlattener(json).withPrintMode(PrintMode.PRETTY).flatten());
// {
//   "abc.def": 123
// }

// JsonUnflattener
// PrintMode.MINIMAL(default)
json = "{\"abc.def\":123}";
System.out.println(new JsonUnflattener(json).withPrintMode(PrintMode.MINIMAL).unflatten());
// {"abc":{"def":123}}

// PrintMode.PRETTY
System.out.println(new JsonUnflattener(json).withPrintMode(PrintMode.PRETTY).unflatten());
// {
//   "abc": {
//     "def": 123
//   }
// }
```
