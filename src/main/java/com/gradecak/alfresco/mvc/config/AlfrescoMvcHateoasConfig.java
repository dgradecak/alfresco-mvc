package com.gradecak.alfresco.mvc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.plugin.core.OrderAwarePluginRegistry;

@Configuration
@EnableSpringDataWebSupport
// @EnableHypermediaSupport
// @EnableAlfrescoRepositories(basePackageClasses = CmDocumentRepository.class)
public class AlfrescoMvcHateoasConfig extends AlfrescoMvcConfig
{

	@Autowired(required = false)
	CurieProvider curieProvider;

	@Autowired
	ListableBeanFactory beanFactory;

	//
	// HAL setup
	//

	@Bean
	@Override
	public HateoasPageableHandlerMethodArgumentResolver pageableResolver()
	{
		return new HateoasPageableHandlerMethodArgumentResolver(sortResolver());
	}

	@Bean
	@Override
	public HateoasSortHandlerMethodArgumentResolver sortResolver()
	{
		return new HateoasSortHandlerMethodArgumentResolver();
	}

	// @Override
	// public void addFormatters(final FormatterRegistry formatterRegistry)
	// {
	// formatterRegistry.addConverter((Converter<String, NodeRef>)nodeRefConverter());
	// }
	//
	// @Bean
	// public NodeRefConverter nodeRefConverter()
	// {
	// return new NodeRefConverter();
	// }

	@Bean
	public MappingJackson2HttpMessageConverter halJacksonHttpMessageConverter()
	{

		final ArrayList<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaTypes.HAL_JSON);

		// Enable returning HAL if application/json is asked if it's configured to be the default type
		// if (config().useHalAsDefaultJsonMediaType()) {
		// mediaTypes.add(MediaType.APPLICATION_JSON);
		// }

		final MappingJackson2HttpMessageConverter converter =
			new TypeConstrainedMappingJackson2HttpMessageConverter(ResourceSupport.class);
		converter.setObjectMapper(halObjectMapper());
		converter.setSupportedMediaTypes(mediaTypes);

		return converter;
	}

	@Bean
	public ObjectMapper halObjectMapper()
	{

		final HalHandlerInstantiator instantiator =
			new HalHandlerInstantiator(getDefaultedRelProvider(), curieProvider);

		final ObjectMapper mapper = objectMapper();
		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(instantiator);

		configureJacksonObjectMapper(mapper);

		return mapper;
	}

	// @Bean
	// public RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor(){
	// RequestResponseBodyMethodProcessor bodyMethodProcessor = new
	// RequestResponseBodyMethodProcessor(defaultMessageConverters());
	//
	// return bodyMethodProcessor;
	// }

	@Bean
	public List<HttpMessageConverter<?>> defaultMessageConverters()
	{

		final List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

		// if (config().getDefaultMediaType().equals(MediaTypes.HAL_JSON)) {
		// messageConverters.add(halJacksonHttpMessageConverter());
		// messageConverters.add(jacksonHttpMessageConverter());
		// } else {
		messageConverters.add(jacksonHttpMessageConverter());
		messageConverters.add(halJacksonHttpMessageConverter());
		// messageConverters.add(formHttpMessageConverter());
		// messageConverters.add(requestResponseBodyMethodProcessor());

		// }

		final MappingJackson2HttpMessageConverter fallbackJsonConverter = new MappingJackson2HttpMessageConverter();
		fallbackJsonConverter.setObjectMapper(objectMapper());

		messageConverters.add(fallbackJsonConverter);
		// messageConverters.add(uriListHttpMessageConverter());

		return messageConverters;
	}

	@Bean
	public DefaultRelProvider defaultRelProvider()
	{
		return new EvoInflectorRelProvider();
	}

	@Bean
	public AnnotationRelProvider annotationRelProvider()
	{
		return new AnnotationRelProvider();
	}

	private RelProvider getDefaultedRelProvider()
	{
		final OrderAwarePluginRegistry<RelProvider, Class<?>> relProviderPluginRegistry =
			OrderAwarePluginRegistry.create(Arrays.asList(defaultRelProvider(), annotationRelProvider()));

		return new DelegatingRelProvider(relProviderPluginRegistry);
	}

	protected void configureHttpMessageConverters(final List<HttpMessageConverter<?>> messageConverters)
	{
	}
}
