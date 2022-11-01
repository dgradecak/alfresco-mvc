The missing glue between Alfresco and Spring MVC
===
Personally, I do not like Alfresco Webscripts because of the boilerplate code that comes with them (XML, FTL, Java/Javascript). Also, I am not a big fan of Javascript on the server side either, as in a medium sized application this becomes unmaintainable. That is why I wrote Alfresco @MVC.

Alfresco @MVC consists of several libraries for REST, AOP
- Alfresco MVC REST enables the usage of the full Spring MVC stack within the context of an Alfresco Webscript, benefiting of Alfresco standard authentication and security
- Alfresco MVC AOP enables simple handling of Alfresco transactions and Alfresco runAs mechanism with simple annotations

[Distributed on Maven Central](https://search.maven.org/search?q=g:com.gradecak.alfresco-mvc)

```
<dependency>
  <groupId>com.gradecak.alfresco-mvc</groupId>
  <artifactId>alfresco-mvc-rest</artifactId>
  <version>xxx</version>
</dependency>

<dependency>
  <groupId>com.gradecak.alfresco-mvc</groupId>
  <artifactId>alfresco-mvc-aop</artifactId>
  <version>xxx</version>
</dependency>
```

Works on Enterprise as well as on Community and it reuses a widely accepted REST framework. 
For the correct version supported by your Alfresco version [please check the wiki](https://github.com/dgradecak/alfresco-mvc/wiki) or 
[The release page](https://github.com/dgradecak/alfresco-mvc/releases).

[The docs are on the wiki page](https://github.com/dgradecak/alfresco-mvc/wiki)

[The Samples are provided on](https://github.com/dgradecak/alfresco-mvc-sample)

You should use it when
-
- You need custom APIs
- You want to be more productive
- You write custom Alfresco Webscripts

You would benefit from
-
- Faster and cleaner development
- Java developers know how to use Spring MVC while newcomers tend to avoid Alfresco Webscripts


For supported Alfresco versions, check the [release notes](https://github.com/dgradecak/alfresco-mvc/releases)

