alfresco @mvc
====
Personally I do not like webscripts because of the boilerplate code that comes with them (XML, FTL, Java/Javascript). Also I am not a big fan of javascript on the server side 
as in a medium sized application that becomes unmaintainable. That is why I wrote Alfresco @MVC.

This small library enables the usage of Spring @MVC within Alfresco. Instead of writing webscripts and all the glue configuration that goes with that, you can simply write Springframework 
Controllers, Services/Components, ... with Spring annotations.

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
for more information please check: com.gradecak.alfresco.mvc.sample.controller.DocumentController

Autowiring of Alfresco and custom dependencies is enabled and thus you may finally have a rapid development with Alfresco.

Json is my preferable way to use for Alfresco integrations and some helpers are also provided (can be seen in the sample application). On the other hand Springframework content negotiation resolver could be use in order to allow different kind of responses.  

A very useful class in this library is com.gradecak.alfresco.mvc.Query. It allows to write alfresco lucene/solr queries in a much simpler way.

New things in 3.0.0
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
public class DocumentNodeMapper implements NodeMapper<Document> {
  public Document mapNode(Map<QName, Serializable> properties) {
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

There is more things to add, so TBC ...

Alfresco versions
----
- Works on Enterprise as well as on community.
- Tested with Alfresco Community 3.4.d, 4.0.x, 4.2.x, 5.0.a
- Tested with Alfresco Enterprise 3.4.5, 4.1.5, 4.2.1

Distribution
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
  <version>3.0.0-SNAPSHOT</version>
</dependency>
```

Latest release version:
```
<dependency>
  <groupId>com.gradecak.alfresco</groupId>
  <artifactId>alfresco-mvc</artifactId>
  <version>2.0.0-RELEASE</version>
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

