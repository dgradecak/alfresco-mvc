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

package com.gradecak.alfresco.mvc.webscript.mock;

import static org.mockito.Mockito.mock;

import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.mock.web.MockHttpServletResponse;

public class MockWebScriptResponse extends WebScriptServletResponse {

  private MockHttpServletResponse mockHttpServletResponse;

  private MockWebScriptResponse(Runtime mockedRuntime, MockHttpServletResponse mockHttpServletResponse) {
    super(mockedRuntime, mockHttpServletResponse);
    this.mockHttpServletResponse = mockHttpServletResponse;
  }

  public MockHttpServletResponse getMockHttpServletResponse() {
    return mockHttpServletResponse;
  }

  static public MockWebScriptResponse createMockWebScriptResponse() {
    return new MockWebScriptResponse(mock(Runtime.class), new MockHttpServletResponse());
  }
}
