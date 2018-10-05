package com.gradecak.alfresco.mvc.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class AlfrescoResponseEntity<T> extends ResponseEntity<T> {

  public static final String MARKER_HEADER = "alfresco-api-marker";
  public static final String MARKER_HEADER_VALUE = Boolean.TRUE.toString();

  public AlfrescoResponseEntity(T body, HttpStatus status) {
    super(body, addAlfrescoMarkerHeader(new HttpHeaders()), status);
  }

  public AlfrescoResponseEntity(HttpStatus status) {
    super(addAlfrescoMarkerHeader(new HttpHeaders()), status);
  }

  public AlfrescoResponseEntity(MultiValueMap<String, String> headers, HttpStatus status) {
    super(null, addAlfrescoMarkerHeader(headers), status);
  }

  public AlfrescoResponseEntity(T body, MultiValueMap<String, String> headers, HttpStatus status) {
    super(body, addAlfrescoMarkerHeader(headers), status);
  }

  private static MultiValueMap<String, String> addAlfrescoMarkerHeader(MultiValueMap<String, String> headers) {
    headers.add(MARKER_HEADER, MARKER_HEADER_VALUE);
    return headers;
  }
}
