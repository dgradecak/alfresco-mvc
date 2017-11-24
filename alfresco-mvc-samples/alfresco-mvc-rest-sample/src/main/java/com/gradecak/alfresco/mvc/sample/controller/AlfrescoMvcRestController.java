package com.gradecak.alfresco.mvc.sample.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * shows how to use the Alfresco @MVC framework
 */

@Controller
@RequestMapping("/rest")
public class AlfrescoMvcRestController {


  @RequestMapping(value = "sample", method = { RequestMethod.GET })
  public ResponseEntity<?> sample() throws IOException {

	  return new ResponseEntity<>("Alfresco @MVC REST sample", HttpStatus.OK);
  }
  
  @RequestMapping(value = "sample", method = { RequestMethod.POST })
  public ResponseEntity<?> sample(@RequestBody final Map<String, String> body) throws IOException {

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
