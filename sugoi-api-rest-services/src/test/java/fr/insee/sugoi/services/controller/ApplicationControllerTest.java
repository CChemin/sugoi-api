/*
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
package fr.insee.sugoi.services.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.model.Application;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationController.class)
@WebMvcTest(value = ApplicationController.class)
@WithMockUser
public class ApplicationControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private ApplicationService applicationService;

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void retrieveAllApplicationsWithoutStorage() {

    Application application1 = new Application();
    application1.setName("SuperAppli");
    application1.setOwner("Amoi");
    Application application2 = new Application();
    application2.setName("SuperAppli2");
    application2.setOwner("Amoi2");
    List<Application> applications = new ArrayList<>();
    applications.add(application1);
    applications.add(application2);
    PageResult<Application> pageResult = new PageResult<Application>();
    pageResult.setResults(applications);

    Mockito.when(applicationService.findByProperties(Mockito.anyString(), Mockito.anyMap()))
        .thenReturn(pageResult);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/toto/applications").accept(MediaType.APPLICATION_JSON);
    try {
      String jsonResult =
          mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString();
      TypeReference<List<Application>> mapType = new TypeReference<List<Application>>() {};
      List<Application> appRes = objectMapper.readValue(jsonResult, mapType);
      assertThat("First element should be SuperAppli", appRes.get(0).getName(), is("SuperAppli"));
      assertThat("SuperAppli should have owner Amoi", appRes.get(0).getOwner(), is("Amoi"));
      assertThat(
          "Second element should be SuperAppli2", appRes.get(1).getName(), is("SuperAppli2"));
      assertThat("SuperAppli2 should have owner Amoi2", appRes.get(1).getOwner(), is("Amoi2"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      fail();
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
