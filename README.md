The missing glue between Alfresco and Spring MVC
===
Works on Enterprise as well as on Community and it ruses a widely accepted REST framework

[The docs are on the wiki page](https://github.com/dgradecak/alfresco-mvc/wiki)

You should use it when
-
- You need custom APIs
- You want to be more productive
- You write custom webscripts

You would benefit of
-
- Faster development
- Java developers know how to use Spring MVC


Supported Alfresco versions
----
v7
-
- the querytemplate module has been deprecated and will be removed in the next major release (Alfresco 5.2+ include a REST API and we recommend using classes from the package org.alfresco.rest.api)
- @AlfrescoRestResponse can be used to annotate a controller or a controller method in order to use Alfresco Rest API response processing
- clearer isolation of Spring MVC Web and specific Jackson processors have been removed since spring has been updated to 5.1.x in Alfresco ACS and Alfresco introduced org.alfresco.rest.framework.jacksonextensions.RestJsonModule which we are reusing now
- use @EnableWebAlfrescoMvc which enables @EnableWebMvc and reuses com.gradecak.alfresco.mvc.rest.config.DefaultAlfrescoMvcServletContextConfig otherwise just use @EnableWebMvc and customize your servlet context as needed
- ResponseMapBuilder has been deprecated use a Map instead
- Tested on Alfresco Community 6.0.7-GA, 6.1.x, 6.2.0-A2
- Tested on Alfresco Enterprise 6.0.1, 6.1

v6
-
- Tested on Alfresco Community 6.0.7-GA, 6.1.x, 6.2.0-A2
- Tested on Alfresco Enterprise 6.0.1, 6.1

v5
-
- Tested on Alfresco Community 3.4.d, 4.0.x, 4.2.x, 5.0.a, 5.0.d, 5.1.e, 5.2.f, 5.2.g (might work with older version, if not please check previous releases/snapshots)
- Tested on Alfresco Enterprise 3.4.5, 4.1.5, 4.2.1, 5.1.x, 5.2.x (might work with older version, if not please check previous releases/snapshots)

v4
-
- Tested on Alfresco Community 3.4.d, 4.0.x, 4.2.x, 5.0.a, 5.0.d, 5.1.e, 5.2.f, 5.2.g (might work with older version, if not please check previous releases/snapshots)
- Tested on Alfresco Enterprise 3.4.5, 4.1.5, 4.2.1, 5.1.x, 5.2.x (might work with older version, if not please check previous releases/snapshots)
