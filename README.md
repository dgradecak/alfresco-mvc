The missing glue between Alfresco and Spring MVC
===
Personally I do not like webscripts because of the boilerplate code that comes with them (XML, FTL, Java/Javascript). Also I am not a big fan of javascript on the server side esither, as in a medium sized application this becomes unmaintainable. That is why I wrote Alfresco @MVC.

Alfresco @MVC consists of several libraries for REST, AOP (and the deprecated QueryTemplate).
- Alfresco MVC REST enables the usage of the full Spring MVC stack within the context of a webscript, benefiting of Alfresco standard authentication and security
- Alfresco MVC AOP enables simple handling of Alfresco transactions and Alfresco runAs mechanism with simple annotations

[Distributed on Maven Central](https://search.maven.org/search?q=g:com.gradecak.alfresco-mvc)

```
<dependency>
  <groupId>com.gradecak.alfresco</groupId>
  <artifactId>alfresco-mvc-rest</artifactId>
  <version>7.5.1-RELEASE</version>
</dependency>

<dependency>
  <groupId>com.gradecak.alfresco</groupId>
  <artifactId>alfresco-mvc-aop</artifactId>
  <version>7.5.1-RELEASE</version>
</dependency>
```

Works on Enterprise as well as on Community and it ruses a widely accepted REST framework

[The docs are on the wiki page](https://github.com/dgradecak/alfresco-mvc/wiki)

You should use it when
-
- You need custom APIs
- You want to be more productive
- You write custom webscripts

You would benefit of
-
- Faster and cleaner development
- Java developers know how to use Spring MVC while new comers tend to avoid webscripts


For supported Alfresco versions check the [release notes](https://github.com/dgradecak/alfresco-mvc/releases)

