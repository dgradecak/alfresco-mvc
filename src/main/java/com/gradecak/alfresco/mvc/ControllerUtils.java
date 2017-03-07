package com.gradecak.alfresco.mvc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControllerUtils
{

	public static <R> ResponseEntity<R> toResponseEntity(final R resource)
	{
		return toResponseEntity(HttpStatus.OK, null, resource);
	}

	public static <R> ResponseEntity<R> toResponseEntity(
		final HttpStatus status, final HttpHeaders headers, final R resource)
	{
		return new ResponseEntity<R>(resource, getHeaders(headers), status);
	}

	public static ResponseEntity<?> toEmptyResponseEntity()
	{
		return toEmptyResponseEntity(HttpStatus.OK);
	}

	public static ResponseEntity<?> toEmptyResponseEntity(final HttpStatus status)
	{
		return new ResponseEntity<>(getHeaders(null), status);
	}

	private static HttpHeaders getHeaders(final HttpHeaders headers)
	{
		final HttpHeaders hdrs = new HttpHeaders();

		if (headers != null)
		{
			hdrs.putAll(headers);
		}

		if (!hdrs.containsKey("Cache-Control"))
		{
			hdrs.add("Cache-Control", "no-cache");
		}

		if (!hdrs.containsKey("Connection"))
		{
			hdrs.add("Connection", "close");
		}

		return hdrs;
	}
}
