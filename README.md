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
or a Java Map
```java
// {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}
```

# Maven Repo
```xml
<dependency>
	<groupId>com.github.wnameless.json</groupId>
	<artifactId>json-flattener</artifactId>
	<version>${newestVersion}</version>
	<!-- Newest version shows in the maven-central badge above -->
</dependency>
```

# Quick Start
```java
String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";

// { "a":
//   { "b": 1,
//     "c": null,
//     "d": [false, true]
//   },
//   "e": "f",
//   "g": 2.3
// }
```

Flatten to Java Map
```java
Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

System.out.println(flattenJson);
// {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}
```

Flatten to JSON string
```java
String jsonStr = JsonFlattener.flatten(json);

System.out.println(jsonStr);
// {"a.b":1,"a.c":null,"a.d[0]":false,"a.d[1]":true,"e":"f","g":2.3}
```

Unflatten from JSON string
```java
String nestedJson = JsonUnflattener.unflatten(jsonStr);

System.out.println(nestedJson);
// {"a":{"b":1,"c":null,"d":[false,true]},"e":"f","g":2.3}
```

Flatten or Unflatten with reserved characters
```java
// Supports JSON keys which contain dots or square brackets
String flattendJsonWithDotKey = JsonFlattener.flatten("[{\"a.a.[\":1},2,{\"c\":[3,4]}]");

System.out.println(flattendJsonWithDotKey);
// {"[0][\"a.a.[\"]":1,"[1]":2,"[2].c[0]":3,"[2].c[1]":4}

String nestedJsonWithDotKey = JsonUnflattener.unflatten(
        "{\"[1][0]\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][\\\"ab.c.[\\\"]\":5}");
	
System.out.println(nestedJsonWithDotKey);
// [1,[2,3],4,{"ab.c.[":5}]
```

# Feature List<a id='top'></a>
| Name | Description | Since |
| --- | --- | --- |
| Jackson, Gson, org.json, Jakarta | Upgrading [json-base](https://github.com/wnameless/json-base) to support 4 major JSON implementations | v0.16.0 |
| [JsonFlattenerFactory](#14.0.1) | produces any JsonFlattener with preconfigured settings | v0.14.0 |
| [JsonUnflattenerFactory](#14.0.2) | produces any JsonUnflattener with preconfigured settings | v0.14.0 |
| [IgnoreReservedCharacters](#13.0.1) | reserved characters in keys can be ignored | v0.13.0 |
| [JsonCore](#12.0.1) | customized JSON libarary(Jackson, GSON, etc.) supported | v0.12.0 |
| [JsonUnflattener.unflatten(Map)](#11.0.1) | new API for Java Map unflattening | v0.11.0 |
| [JsonUnflattener.unflattenAsMap](#11.0.2) | new API for JSON unflattening | v0.11.0 |
| [JsonFlattener.flattenAsMap(JsonValueBase)](#8.1.1) | new API for JSON flattening | v0.8.1 |
| [FlattenMode.KEEP_PRIMITIVE_ARRAYS](#8.0.1) | new FlattenMode to keep all primitive JSON arrrays | v0.8.0 |
| [JsonValueBase](#7.0.1)| comes from json-base lib, is introduced to improve performance | v0.7.0 |
| [StringEscapePolicy](#6.0.1) | ALL, ALL_BUT_SLASH, ALL_BUT_UNICODE, ALL_BUT_SLASH_AND_UNICODE, DEFAULT | v0.6.0 |
| [CharSequenceTranslatorFactory](#5.0.1) | customized StringEscapePolicy | v0.5.0 |
| [FlattenMode.MONGODB](#4.0.1) | dot notation | v0.4.0 |
| [KeyTransformer](#4.0.2) | manipulates keys before flattening  | v0.4.0 |
| [LeftAndRightBrackets](#3.0.1) | customized brackets | v0.3.0 |
| [Reader](#3.0.2) | input JSON as Java Reader | v0.3.0 |
| [FlattenMode](#2.0.1) | NORMAL, KEEP_ARRAYS | v0.2.0 |
| [StringEscapePolicy](#2.0.2) | NORMAL, ALL_UNICODES | v0.2.0 |
| [Separator](#2.0.3) | customized separator | v0.2.0 |
| [PrintMode](#2.0.4) | MINIMAL, PRETTY | v0.2.0 |

### [:top:](#top) JsonFlattenerFactory<a id='14.0.1'></a> - produces any JsonFlattener with preconfigured settings
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
  JsonFlattener jf2 = jsonFlattenerFactory.build(new StringReader(json));
  JsonFlattener jf3 = jsonFlattenerFactory.build(new GsonJsonCore().parse(json));
}
```

### [:top:](#top) JsonUnflattenerFactory<a id='14.0.2'></a> - produces any JsonUnflattener with preconfigured settings
```java
// Inside Spring configuration class
@Bean
public JsonUnflattenerFactory jsonUnflattenerFactory() {
  // Sets the FlattenMode to MONGODB
  Consumer<JsonUnflattener> configurer = ju -> ju.withFlattenMode(FlattenMode.MONGODB);
  // Alters the default JsonCore from Jackson to GSON
  JsonCore<?> jsonCore = new GsonJsonCore();

  return new JsonUnflattenerFactory(configurer, jsonCore);
}

// In any other Spring environment class
@Autowired
JsonUnflattenerFactory jsonUnflattenerFactory;

public void usageExamples(String json) {
  JsonUnflattener ju1 = jsonUnflattenerFactory.build(json);
  JsonUnflattener ju2 = jsonUnflattenerFactory.build(new StringReader(json));
  JsonUnflattener ju3 = jsonUnflattenerFactory.build((Map<String, ?>) new ObjectMapper().readValue(json, Map.class));
}
```

### [:top:](#top) IgnoreReservedCharacters<a id='13.0.1'></a> - reserved characters in keys can be ignored
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

### [:top:](#top) JsonCore<a id='12.0.1'></a> - customized JSON libarary supported
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

### [:top:](#top) JsonUnflattener.unflatten(Map)<a id='11.0.1'></a> - new API for Java Map unflattening
```java
String json = "{\"abc\":{\"def\":[1,2,{\"g\":{\"h\":[3]}}]}}";
Map<String, Object> flattenedMap = JsonFlattener.flattenAsMap(json);

String unflattenedJson = JsonUnflattener.unflatten(flattenedMap);
```

### [:top:](#top) JsonUnflattener.unflattenAsMap<a id='11.0.2'></a> - new API for JSON unflattening
```java
String json = "{\"abc\":{\"def\":[1,2,{\"g\":{\"h\":[3]}}]}}";
String flattenedJson = JsonFlattener.flatten(json);
Map<String, Object> flattenedMap = JsonFlattener.flattenAsMap(json);

Map<String, Object> unflattenedMap;
unflattenedMap = JsonUnflattener.unflattenAsMap(flattenedJson);
unflattenedMap = JsonUnflattener.unflattenAsMap(flattenedMap);
```

### [:top:](#top) JsonFlattener.flattenAsMap(JsonValueBase)<a id='8.1.1'></a> - new API for JSON flattening
```java
JsonValueBase<?> jsonVal;

// JacksonJsonValue, which is provided by json-base lib, can wrap Jackson jsonNode to JsonValueBase
jsonVal = new JacksonJsonValue(jsonNode);

Map<String, Object> flattenMap = JsonFlattener.flattenAsMap(jsonVal);
```

### [:top:](#top) FlattenMode.KEEP_PRIMITIVE_ARRAYS<a id='8.0.1'></a> - new FlattenMode to keep all primitive JSON arrrays
```java
String json = "{\"ary\":[true,[1, 2, 3],false]}";
System.out.println(new JsonFlattener(json).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS).flatten());
// {"ary[0]":true,"ary[1]":[1,2,3],"ary[2]":false}
```
This mode only keeps arrays which contain only primitive types(strings, numbers, booleans, and null).

### [:top:](#top) JsonValueBase<a id='7.0.1'></a> - comes from json-base lib, is introduced to improve performance
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

### [:top:](#top) StringEscapePolicy<a id='6.0.1'></a> - ALL, ALL_BUT_SLASH, ALL_BUT_UNICODE, ALL_BUT_SLASH_AND_UNICODE, DEFAULT
```java
StringEscapePolicy.ALL //                       Escapes all JSON special characters and Unicode
StringEscapePolicy.ALL_BUT_SLASH //             Escapes all JSON special characters and Unicode but slash('/')
StringEscapePolicy.ALL_BUT_UNICODE //           Escapes all JSON special characters but Unicode
StringEscapePolicy.ALL_BUT_SLASH_AND_UNICODE // Escapes all JSON special characters but slash('/') and Unicode
StringEscapePolicy.DEFAULT //                   Escapes all JSON special characters but slash('/') and Unicode
```

### [:top:](#top) CharSequenceTranslatorFactory<a id='5.0.1'></a> - customized StringEscapePolicy
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

### [:top:](#top) FlattenMode.MONGODB<a id='4.0.1'></a> - dot notation
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

### [:top:](#top) KeyTransformer<a id='4.0.2'></a> - manipulates keys before flattening 
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

### [:top:](#top) LeftAndRightBrackets<a id='3.0.1'></a> - customized brackets
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

### [:top:](#top) Reader<a id='3.0.2'></a> - input JSON as Java Reader
```java
InputStream inputStream = new FileInputStream("simple.json");
Reader reader = new InputStreamReader(inputStream);

// Support Reader as input 
JsonFlattener jf = new JsonFlattener(reader);
JsonUnflattener ju = new JsonUnflattener(reader);
```

### [:top:](#top) FlattenMode<a id='2.0.1'></a> - NORMAL, KEEP_ARRAYS
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

### [:top:](#top) StringEscapePolicy<a id='2.0.2'></a> - NORMAL, ALL_UNICODES
```java
String json = "{\"abc\":{\"def\":\"太極\\t\"}}";

// StringEscapePolicy.NORMAL(default) - escape only speacial characters
System.out.println(new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.NORMAL).flatten());
// {"abc.def":"太極\t"}

// StringEscapePolicy.ALL_UNICODES - escape speacial characters and unicodes
System.out.println(new JsonFlattener(json).withStringEscapePolicy(StringEscapePolicy.ALL_UNICODES).flatten());
// {"abc.def":"\u592A\u6975\t"}
```

### [:top:](#top) Separator<a id='2.0.3'></a> - customized separator
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

### [:top:](#top) PrintMode<a id='2.0.4'></a> - MINIMAL, PRETTY
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

## MISC
| Note| Since |
| --- | --- |
| Java 8 required. | v0.5.0 |
| StringEscapePolicy.DEFAULT, which escapes all special characters but slash('/') and Unicode, becomes the default setting. | v0.6.0 |
| Group ID is changed from [com.github.wnameless] to [com.github.wnameless.json]. | v0.7.0 |
| Java Module supported. | v0.9.0 |
| The Map produced by JsonFlattener#flattenAsMap after serialization is now identical to the JSON produced by JsonFlattener#flatten. Before v0.10.0, the serialized flattened Map may be different at some edge cases(ex: input keys contain separator('.')). | v0.10.0 |
