[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless/json-flattener/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless/json-flattener)

json-flattener
=============
A Java utility used to FLATTEN nested JSON objects and even more to UNFLATTEN it back.

##Purpose
Converts a nested JSON<br />
&nbsp;&nbsp;{ "a": { "b": 1, "c": null, "d": [false, true] }, "e": "f", "g": 2.3 }<br />
into a flattened JSON<br />
&nbsp;&nbsp;{ "a.b": 1, "a.c": null, "a.d[0]": false, "a.d[1]": true, "e": f, "g": 2.3 }<br />
or a Java Map<br />
&nbsp;&nbsp;{a.b=1, a.c=null, a.d[0]=false, a.d[1]=true, e=f, g=2.3}

#Maven Repo
```xml
<dependency>
	<groupId>com.github.wnameless</groupId>
	<artifactId>json-flattener</artifactId>
	<version>0.1.2</version>
</dependency>
```


###Quick Start
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
System.out.println(flatteflattendJsonWithDotKeynJsonWithDotKey);
// Output: {"[0][\"a.a.[\"]":12,"[1]":2,"[2].c[0]":3,"[2].c[1]":4}

String nestedJsonWithDotKey = JsonUnflattener.unflatten(
        "{\"[1][0];\":2,\"[0]\":1,\"[1][1]\":3,\"[2]\":4,\"[3][\\\"ab.c.[\\\"]\":5}");
System.out.println(nestedJsonWithDotKey);
// Output: [1,[2,3],4,{"ab.c.[":5}]
```
