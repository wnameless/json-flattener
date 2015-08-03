json-flattener
=============
A Java utility to flatten nested JSON objects.

##Purpose
Converts a nested JSON<br />
  { "a": { "b": 1, "c": null, "d": [false, true] }, "e": "f", "g": 2.3 }<br />
into simple Java Map<br />
  {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

#Maven Repo
```xml
Not publish yet
```


###Quick Start
Class with 0-argument constructor
```java
String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";
Map<String, Object> flattenJson = new JsonFlattener(json).flattenAsMap();

System.out.println(flattenJson);
// Output: {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

String jsonStr = new JsonFlattener(json).flatten();
System.out.println(jsonStr);
// Output: {"a.b":1,"a.c":null,"a.d[0]":false,"a.d[1]":true,"e":"f","g":2.3}

String nestedJson = JsonUnflattener.unflatten(jsonStr);
System.out.println(nestedJson);
// {"a":{"b":1,"c":null,"d":[false,true]},"e":"f","g":2.3}
```
