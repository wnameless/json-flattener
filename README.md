json-flattener
=============
A Java utility to flatten nested JSON objects.

##Purpose
Converts a nested JSON<br />
  { "a" : { "b" : 1, "c": null, "d": [false, true] }, "e": "f" }<br />
into simple Java Map<br />
  {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f}

#Maven Repo
```xml
Not publish yet
```


###Quick Start
Class with 0-argument constructor
```java
String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\"}";
Map<String, Object> flattenJson = new JsonFlattener(json).flatten();

System.out.println(flattenJson);
// Output: {a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f}
```
