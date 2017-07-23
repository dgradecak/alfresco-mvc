package com.gradecak.alfresco.mvc.rest.sample;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
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

    this.mockMvc.perform(get("/rest/sample").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andDo(document("/rest/sample", preprocessResponse(prettyPrint())));
  }
}
