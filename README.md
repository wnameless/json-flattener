json-flattener
=============
A Java utility used to flatten nested JSON objects and even more to unflatten it back.

##Purpose
Converts a nested JSON<br />
&nbsp;&nbsp;{ "a": { "b": 1, "c": null, "d": [false, true] }, "e": "f", "g": 2.3 }<br />
into simple Java Map<br />
&nbsp;&nbsp;{a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

#Maven Repo
```xml
Not publish yet
```


###Quick Start
Class with 0-argument constructor
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
```
