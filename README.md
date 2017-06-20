[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless/json-flattener/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless/json-flattener)
[![codecov](https://codecov.io/gh/wnameless/json-flattener/branch/master/graph/badge.svg)](https://codecov.io/gh/wnameless/json-flattener)

json-flattener
=============
A Java utility used to FLATTEN nested JSON objects and even more to UNFLATTEN it back

## Purpose
Converts a nested JSON<br />
&nbsp;&nbsp;{ "a": { "b": 1, "c": null, "d": [false, true] }, "e": "f", "g": 2.3 }<br />
into a flattened JSON<br />
&nbsp;&nbsp;{ "a.b": 1, "a.c": null, "a.d[0]": false, "a.d[1]": true, "e": f, "g": 2.3 }<br />
or a Java Map<br />
&nbsp;&nbsp;{a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

## Maven Repo
```xml
<dependency>
	<groupId>com.github.wnameless</groupId>
	<artifactId>json-flattener</artifactId>
	<version>0.4.0</version>
</dependency>
```

This version is used to solve the conflict("missing ESCAPE_JSON field") between Apache common-lang3 and common-lang.<br />
If you don't face such a problem, you should use the original version instead.<br />
BTW, this version needs Java 7 obviously.
```xml
<dependency>
	<groupId>com.github.wnameless</groupId>
	<artifactId>json-flattener-java7</artifactId>
	<version>0.4.0</version>
</dependency>
```


## Quick Start
```java
String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";
Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

System.out.println(flattenJson);
// Output: {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

String jsonStr = JsonFlattener.flatten(json);
System.out.println(jsonStr);
// Output: {"a.b":1,"a.c":null,"a.d[0]":false,"a.d[1]":true,"e":"f","g":2.3}

String nestedJson = JsonUnflattener.unflatten(jsonStr);
System.out.println(nestedJson);
// {"a":{"b":1,"c":null,"d":[false,true]},"e":"f","g":2.3}

// Support JSON keys which contain dots or square brackets
String flattendJsonWithDotKey = JsonFlattener.flatten("[{\"a.a.[\":1},2,{\"c\":[3,4]}]");
System.out.println(flattendJsonWithDotKey);
// Output: {"[0][\"a.a.[\"]":12,"[1]":2,"[2].c[0]":3,"[2].c[1]":4}

String nestedJsonWithDotKey = JsonUnflattener.unflatten(
        "{\"[1][0];\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][\\\"ab.c.[\\\"]\":5}");
System.out.println(nestedJsonWithDotKey);
// Output: [1,[2,3],4,{"ab.c.[":5}]
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

// With FlattenMode.MONGODB, separator can sill be changed
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

// PrintMode.REGULAR
System.out.println(new JsonFlattener(json).withPrintMode(PrintMode.REGULAR).flatten());
// { "abc.def": 123 }

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

// PrintMode.REGULAR
System.out.println(new JsonUnflattener(json).withPrintMode(PrintMode.REGULAR).unflatten());
// {"abc": {"def": 123}}

// PrintMode.PRETTY
System.out.println(new JsonUnflattener(json).withPrintMode(PrintMode.PRETTY).unflatten());
// {
//   "abc": {
//     "def": 123
//   }
// }
```
