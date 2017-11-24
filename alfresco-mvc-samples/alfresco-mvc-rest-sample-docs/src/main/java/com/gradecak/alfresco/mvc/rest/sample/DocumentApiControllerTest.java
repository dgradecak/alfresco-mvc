package com.gradecak.alfresco.mvc.rest.sample;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.gradecak.alfresco.mvc.sample.controller.AlfrescoMvcRestController;

public class DocumentApiControllerTest {

//  @Mock
//  private CoreDocumentService documentService;
//
//  @Mock
//  private NoteService noteService;

  @InjectMocks
  private AlfrescoMvcRestController indexController;

  private MockMvc mockMvc;

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

  private AuthenticationUtil util = new AuthenticationUtil();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    this.mockMvc = MockMvcBuilders.standaloneSetup(indexController).apply(documentationConfiguration(this.restDocumentation)).build();
    util.afterPropertiesSet();
  }

  @Test
  public void getDocument() throws Exception {
    //when(documentService.get(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "4"))).thenReturn(ApiUtils.createDocument());

    this.mockMvc.perform(get("/api/flow/get?username=test&id=4").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andDo(document("/api/flow/get", preprocessResponse(prettyPrint()),
            requestParameters(parameterWithName("username").description("the username"), parameterWithName("id").description("flow document id to be retreived")),
            responseFields(fieldWithPath("success").description("status of the request handling"), fieldWithPath("total").description("number of items returned"),
                fieldWithPath("data").description("represents the flow document"), fieldWithPath("data.created").description("represents the flow document"))));
  }
}
