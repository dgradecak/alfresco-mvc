since Alfresco MVC 5.0.0
---
- a new project structure is created
- no alfresco modules are registered when the jar/amp files are deployed
- include samples
- include a deployable alfresco project (alfresco-mvc-samples-bom/alfresco-5.2.e). You need to configure the database and alf_data
- an AMP artifact can be build from the source code (mvn clean package). You will find it under alfresco-mvc-dist/target. However, it is better to include the dependencies in your build

Personally I do not like webscripts because of the boilerplate code that comes with them (XML, FTL, Java/Javascript). Also I am not a big fan of javascript on the server side as in a medium sized application that becomes unmaintainable. That is why I wrote Alfresco @MVC.

Alfresco @MVC consist of several libraries, REST, AOP, QueryTemplate, Spring-DATA. REST and AOP have no third party dependencies, where QueryTemplate and Spring-DATA do have. 

Alfresco-MVC DATA
===
A proof of concept implementation of Spring Data on top of Alfresco. With the combination of Alfresco MVC and Spring Data, developers benefit of a incredible rapid development library.

Define the Alfresco Spring Data repository
```
@Repository
public interface CmDocumentRepository extends AlfrescoNodeRepository<CmDocument> {}
```

A new annotation helping to identify and describe Alfresco nodes (for now there is a naming convention camel cased QName is the field name, or use a custom mapper as in Alfresco MVC queryTemplate)
```
@AlfrescoNode
public class CmDocument extends AbstractPersistable {

  private String cmVersionLabel;
  private String cmTitle;
  private String cmDescription;
  
  ...
}

@AlfrescoNode(entityMapper = CmDocumentPropertiesMapper.class, nodeMapper = CmDocumentPropertiesMapper.class)
```

Enable the repositories with @EnableAlfrescoRepositories in spring's configuration
```
@Configuration
@EnableAlfrescoRepositories(basePackageClasses = CmDocumentRepository.class)
public class AlfrescoMvcDataModuleConfig extends AlfrescoMvcDataConfig{
}
```


Alfresco-MVC REST
===
This small library enables the usage of Spring @MVC within Alfresco. Instead of writing webscripts and all the glue configuration that goes with that, you can simply write Springframework Controllers, Services/Components, ... with Spring annotations.

```
@Controller
@RequestMapping("/document")
public class DocumentController {

	@Autowired
	private SomeService service;

	@RequestMapping(value = "{id}", method = { RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<?> index(@@PathVariable Long id) {
	  // yes this works in Alfresco
	  return new ResponseEntity<?>(service.get(id), HttpStatus.OK);
	}
}
```

This library offers some out of the box configurations for webscript descriptor bindings. The entry endpoint is by default /alfresco/service/mvc only if the DispatcherWebscript is configured with bean names as follows: "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get", "webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put"

otherwise you are free to create your own webscript descriptors and configure the beans correctly.


A DispatcherWebscript can be connfigured :

with a servlet-conext.xml file
--- 
```
  <bean id="webscript.alfresco-mvc.mvc.post" class="com.gradecak.alfresco.mvc.webscript.DispatcherWebscript" parent="webscript">
    <property name="contextConfigLocation" value="classpath:alfresco/module/YOUR_MODULE/context/servlet-context.xml" />
  </bean>
  
  <alias name="webscript.alfresco-mvc.mvc.post" alias="webscript.alfresco-mvc.mvc.get" />
```

with Java config
---
```
  @Bean(name = { "webscript.alfresco-mvc.mvc.post", "webscript.alfresco-mvc.mvc.get", "webscript.alfresco-mvc.mvc.delete", "webscript.alfresco-mvc.mvc.put" })
  public DispatcherWebscript dispatcherWebscript() {
    DispatcherWebscript dispatcherWebscript = new DispatcherWebscript();
    dispatcherWebscript.setContextClass(org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class);
    dispatcherWebscript.setContextConfigLocation(AlfrescoMvcHateoasConfig.class.getName());
    return dispatcherWebscript;
  }
```

AlfrescoMvcHateoasConfig has to be a spring' @Configuration class and please note the contextClass property should be AnnotationConfigWebApplicationContext
```
  @Configuration
  @ComponentScan(basePackageClasses = { "...controller" })
  @EnableWebMvc
  public class AlfrescoMvcHateoasConfig {
    ...
  }
```

=> The library is mainly used on the alfresco repository side, but is also suitable for alfresco share.


Alfresco-MVC AOP
===
Enables a couple of useful annotations on the alfresco repository side.

@AlfrescoAuthentication
---
  used on a service method to indicate what type of authentication is allowed, same usage as in the webscript decriptor <authentication>user</authentication>.
  Four possibilities are   NONE, GUEST, USER, ADMIN as defined in the AuthenticationType enum. Defaults to USER
  
@AlfrescoRunAs
---
  allows with a simple annotation to use the runas mechanism of alfresco. The value has to be a static string with the username. 

@AlfrescoTransaction
---
   this one uses the RetryingTansaction in order to avoid to write all lines for a RetryingTransactionCallback, Params: readOnly defaults to true and 
   propagation defaults to org.springframework.transaction.annotation.Propagation.REQUIRED

Configuration:

XML
---
```
  <context:component-scan base-package="com.gradecak.alfresco.mvc.sample.service">
    <context:include-filter expression="org.springframework.stereotype.Service" type="annotation" />
  </context:component-scan>

  <bean class="com.gradecak.alfresco.mvc.aop.PackageAutoProxyCreator">
    <property name="basePackage" value="com.gradecak.alfresco.mvc.sample.service" />
  </bean>
```

Java Config
---
```
@ComponentScan(basePackageClasses = { "com.gradecak.alfresco.mvc.sample.service" })
@EnableAlfrescoMvcProxy(basePackageClasses = { "com.gradecak.alfresco.mvc.sample.service" })
```

EnableAlfrescoMvcProxy or PackageAutoProxyCreator will auto create spring's proxies for all the classes in the specified package in order to apply the advices 

Notice
-
Some issues while using CMIS were spotted if the services are registered via @ComponentScan and used in alfresco behaviors, therefore for now it is recommended to use the xml or java (@Bean) config for service scanning only. This is due to the lifecycle of spring's loading of beans.

There is a spring proxy limitation (spring 3.2.x) in order to use @Autowired on constructors, therefore @Autowired for now should be used on fields. This is due to how CGLIB creates the proxies (a default constructor is needed)

Alfresco-MVC QUERY TEMPLATE
===
Inspired by spring's jdbc/rest templates this is a very useful way of writing alfresco lucene/solr queries (not canned queries). Has a dependencies on Srring Data Commons.
The QueryTemplate class is thread safe and it is advised to use it as singleton.
 
```
new Query().type(Qname).or().property(Qname).and(...)...
```

There is also a mapper mechanism that allows mapping to POJO classes
```
public class DocumentNodeMapper implements NodePropertiesMapper<Document> {
  private final ServiceRegistry serviceRegistry;

  public DocumentNodeMapper(final ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
  
  public Document mapNode(NodeRef nodeRef, Map<QName, Serializable> properties) {
    Document doc = new Document();
	doc.setDescription((String) properties.get(ContentModel.PROP_DESCRIPTION));
	
	ContentReader reader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
    if (reader != null && reader.exists()) {
      doc.setSize(reader.getSize());
    }
	
	
	return doc;
  }	
}

Document document = new QueryTemplate(serviceRegistry).queryForObject(NodeRef, new DocumentNodeMapper(serviceRegistry));

List<Document> documentList = new QueryTemplate(serviceRegistry).queryForList(new Query().type(ContentModel.TYPE_CONTENT), new DocumentNodeMapper(serviceRegistry));
```

Supported Alfresco versions
----
- Tested with Alfresco Community 5.0.d, 5.2.f (might work with older version, if not please check previous releases/snapshots)
- Tested with Alfresco Enterprise 5.1 (might work with older version, if not please check previous releases/snapshots)


Sample Applications
----
Alfresco @MVC comes with a couple of sample applications that are located in /alfresco-mvc-samples-bom


- alfresco-mvc-rest-sample 			=> http://localhost:8080/alfresco/service/mvc/rest/sample
- alfresco-mvc-aop-sample 			=> http://localhost:8080/alfresco/service/mvc/aop/sample
- alfresco-mvc-querytemplate-sample => http://localhost:8080/alfresco/service/mvc/querytemplate/sample 
                                       http://localhost:8080/alfresco/service/mvc/querytemplate/search (search engine has to be enabled)
                                       
- alfresco-mvc-data-sample 			=> http://localhost:8080/alfresco/service/mvc/data/document 
                                       http://localhost:8080/alfresco/service/mvc/folder

alfresco-mvc-querytemplate-sample is the most complete sample that reassemble all the features of Alfresco @MVC

Maven dependency:
----
Latest snapshot version comes with a BOM:
```
  <dependencyManagement>
  	<dependencies>
  	  <dependency>
  	    <groupId>com.gradecak.alfresco-mvc</groupId>
  	  	 <artifactId>alfresco-mvc-bom</artifactId>
  	  	 <version>5.0.0-SNAPSHOT</version>
  	  	 <type>pom</type>
  	  	 <scope>import</scope>
  	  </dependency>
  	</dependencies>
  </dependencyManagement>

  <dependency>
  	<groupId>com.gradecak.alfresco-mvc</groupId>
  	<artifactId>alfresco-mvc-rest</artifactId>
  	<scope>compile</scope>
  </dependency>
  
  <dependency>
  	<groupId>com.gradecak.alfresco-mvc</groupId>
  	<artifactId>alfresco-mvc-aop</artifactId>
  	<scope>compile</scope>
  </dependency>
  
  <dependency>
  	<groupId>com.gradecak.alfresco-mvc</groupId>
  	<artifactId>alfresco-mvc-querytemplate</artifactId>
  	<scope>compile</scope>
  </dependency>
```

Latest release version:
```
  <dependency>
    <groupId>com.gradecak.alfresco</groupId>
    <artifactId>alfresco-mvc</artifactId>
    <version>4.5.0-RELEASE</version>
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

