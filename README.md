alfresco @mvc
====
Personally I do not like webscripts because of the boilerplate code that comes with them (XML, FTL, Java/Javascript). Also I am not a big fan of javascript on the server side 
as in a medium sized application that becomes unmaintainable. That is why I wrote Alfresco @MVC.

This small library enables the usage of Spring @MVC within Alfresco. Instead of writing webscripts and all the glue configuration that goes with that, you can simply write Springframework 
Controllers, Services/Components, ... with Spring annotations.

This library is deployed as an alfresco module (jar packaged) and offers some out of the box configurations for webscript bindings. The entry endpoint is bydefault /mvc only if the DispatcherWebscript
is configured as follows.


```
<bean id="webscript.alfresco-mvc.mvc.post" class="com.gradecak.alfresco.mvc.webscript.DispatcherWebscript" parent="webscript">
    <property name="contextConfigLocation" value="classpath:alfresco/module/YOUR-MODULE/context/servlet-context.xml" />
</bean>
```

Surely, you can configure any other webscript descriptor.

in the servlet-context you can simply use
```
  <mvc:annotation-driven />
```

another utility in order to auto proxy all your services and add the 3 spring AOP interceptors would be  (check the type="annotation" in component scanning)

```
  <bean id="my.autowiredProcessor" class="org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor" />

  <context:component-scan base-package="com.gradecak.alfresco.sample.service" annotation-config="false">
    <context:include-filter expression="org.springframework.stereotype.Service" type="annotation" />
  </context:component-scan>
  
  <bean id="my.services" class="com.gradecak.alfresco.mvc.aop.PackageAutoProxyCreator">
    <property name="basePackage" value="com.gradecak.alfresco.sample.service" />
  </bean>
```


```@Controller
@RequestMapping("/document/**")
public class DocumentController {

	@Autowired
	private ServiceRegistry serviceRegistry;

	@RequestMapping(value = "sample", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Map<String, Object> index(@RequestBodyfinal Document content) {
	  // yes this works in Alfresco
	}
}
```
The invoke URL for the above sample would be:
 - http://localhost:8080/share/proxy/alfresco/mvc/document/sample
 - http://localhost:8080/localhost/alfresco/service/mvc/document/sample

for more information please check: com.gradecak.alfresco.mvc.sample.controller.DocumentController

Autowiring of Alfresco and custom dependencies is enabled and thus you may finally have a rapid development with Alfresco.

Json is my preferable way to use for Alfresco integrations and some helpers are also provided (can be seen in the sample application). On the other hand Springframework content negotiation resolver could be use in order to allow different kind of responses.  

A very useful class in this library is com.gradecak.alfresco.mvc.Query. It allows to write alfresco lucene/solr queries in a much simpler way.

New things in 3.0.3-SNAPSHOT
---
- moved Query class in the Alfresco @MVC package
- moved JsonUtils in the Alfresco @MVC package
- deprecating Alfresco-Simpless project
- introducing NodeMapper for POJO mapping to alfresco properties and QueryTemplate

```
Query query = new Query().path("some path").and().type(Qname).or()...
String q = query.toString(); 
```

Mapping to POJO
```
public class DocumentNodeMapper implements NodePropertiesMapper<Document> {
  public Document mapNode(NodeRef nodeRef, Map<QName, Serializable> properties) {
    Document dl = new Document();
	dl.setDescription((String) properties.get(ContentModel.PROP_DESCRIPTION));
	return dl;
  }	
}
```
The mapper is used in querying with com.gradecak.alfresco.mvc.mapper.QueryTemplate
```
Document document = new QueryTemplate(serviceRegistry).queryForObject(ref, new DocumentNodeMapper());
```

```
List<Document> documentList = new QueryTemplate(serviceRegistry).queryForList(new Query().type(ContentModel.TYPE_CONTENT), new DocumentNodeMapper());
```

Annotations (AOP Advices/Spring interceptors)
----
there are 3 annotations that come with this library.

- @AlfrescoAuthentication
  used on a service method to indicate what type of authentication is allowed, same usage as in the webscript decriptor <authentication>user</authentication>.
  Four possibilities are   NONE, GUEST, USER, ADMIN as defined in the AuthenticationType enum. Defaults to USER
- @AlfrescoRunAs
  allows with a simple annotation to use the runas mechanism of alfresco. The value has to be a static string with the username. 
- @AlfrescoTransaction
   this one uses the RetryingTansaction in order to avoid to write all lines for a RetryingTransactionCallback, Params: readOnly defaults to true and 
   propagation defaults to org.springframework.transaction.annotation.Propagation.REQUIRED


There is more things to add, so TBC ...

Alfresco versions
----
- Works on Enterprise as well as on community.
- Tested with Alfresco Community 3.4.d, 4.0.x, 4.2.x, 5.0.a, 5.0.d
- Tested with Alfresco Enterprise 3.4.5, 4.1.5, 4.2.1, 5.1

Distribution (TODO as it is not yet inline with the latests @MVC library)
----
Alfresco @MVC comes with a sample application: https://github.com/dgradecak/alfresco-mvc-sample
and is distributed as a JAR file (actually AMP).

Maven dependency:
----
Latest snapshot version:
```
<dependency>
  <groupId>com.gradecak.alfresco</groupId>
  <artifactId>alfresco-mvc</artifactId>
  <version>3.0.3-SNAPSHOT</version>
</dependency>
```

Latest release version:
```
<dependency>
  <groupId>com.gradecak.alfresco</groupId>
  <artifactId>alfresco-mvc</artifactId>
  <version>3.0.0-RELEASE</version>
</dependency>
```

Maven repositories:
----
```
<repositories>
  <repository>
    <id>gradecak</id>
    <url>http://gradecak.com/repository/releases/</url>
  </repository>
  <repository>
    <id>gradecak-snapshots</id>
    <url>http://gradecak.com/repository/snapshots/</url>
  </repository>
</repositories>
```

