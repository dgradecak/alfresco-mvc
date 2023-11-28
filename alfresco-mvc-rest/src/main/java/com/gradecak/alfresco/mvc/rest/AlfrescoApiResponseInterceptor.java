/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.mvc.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.springframework.core.MethodParameter;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoRestResponse;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.WebscriptRequestWrapper;

/**
 * 
 * class used to process the response with alfresco rest API behavior only if
 * the annotation {@link AlfrescoRestResponse} is used
 */
@ControllerAdvice
public class AlfrescoApiResponseInterceptor implements ResponseBodyAdvice<Object> {

	private final ResourceWebScriptHelper webscriptHelper;
	private final boolean globalAlfrescoResponse;;

	public AlfrescoApiResponseInterceptor(final ResourceWebScriptHelper webscriptHelper) {
		this(webscriptHelper, false);
	}

	public AlfrescoApiResponseInterceptor(final ResourceWebScriptHelper webscriptHelper,
			final boolean globalAlfrescoResponse) {
		this.webscriptHelper = webscriptHelper;
		this.globalAlfrescoResponse = globalAlfrescoResponse;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		if (!(request instanceof ServletServerHttpRequest)) {
			throw new RuntimeException(
					"the request must be an instance of org.springframework.http.server.ServletServerHttpRequest");
		}

		HttpServletRequest r = ((ServletServerHttpRequest) request).getServletRequest();

		if (!(r instanceof WebscriptRequestWrapper)) {
			throw new RuntimeException(
					"the request must be an instance of com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.WebscriptRequestWrapper. It seems the request is not coming from Alfresco @MVC");
		}

		WebScriptServletRequest a = ((WebscriptRequestWrapper) r).getWebScriptServletRequest();

		return webscriptHelper.processAdditionsToTheResponse(null, null, null, getDefaultParameters(a), body);
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		if (converterType.isAssignableFrom(MappingJackson2HttpMessageConverter.class)) {
			boolean useAlfrescoResponse = globalAlfrescoResponse;

			if (!useAlfrescoResponse) {
				AlfrescoRestResponse methodAnnotation = returnType.getMethodAnnotation(AlfrescoRestResponse.class);
				if (methodAnnotation == null) {
					methodAnnotation = returnType.getContainingClass().getAnnotation(AlfrescoRestResponse.class);
				}

				if (methodAnnotation != null) {
					useAlfrescoResponse = true;
				}
			}
			return useAlfrescoResponse;
		}
		return false;
	}

	static public Params getDefaultParameters(WebScriptRequest wsr) {
		if (wsr != null) {
			final RecognizedParams params = new AlfrescoRecognizedParamsExtractor().getRecognizedParams(wsr);
			return Params.valueOf(params, null, null, wsr);
		}
		Params parameters = Params.valueOf("", null, null);
		return parameters;
	}
}
