package com.gradecak.alfresco.mvc.controller;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice(assignableTypes = TestController.class)
public class TestControllerAdvice implements ResponseBodyAdvice<NodeRef> {

	@Override
	public NodeRef beforeBodyWrite(NodeRef body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		response.getHeaders().add("TEST_ADVICE_APPLIED", "true");
		return new NodeRef("a://a/b");

	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return converterType.isAssignableFrom(MappingJackson2HttpMessageConverter.class);
	}

}
