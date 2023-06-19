/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IStatusCheckClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.ITransactionInspectCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.TransactionInspectResDTO;


@AutoConfigureMockMvc
@ActiveProfiles(Constants.Profile.TEST)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionInspectTest {

	@Autowired
	private ServletWebServerApplicationContext webServerAppCtxt;
	
	@Autowired
	ITransactionInspectCTL txInspectCtl; 
	
	@SpyBean
	private IStatusCheckClient statusCheckClient; 
	
	@Autowired
	public MockMvc mvc; 
	
	@SpyBean
	RestTemplate restTemplate; 
	
	@Test
	@DisplayName("Call Search Event By wid Test")
	void callSearchEventByWorkflowIdTest() throws Exception {
		TransactionInspectResDTO res = new TransactionInspectResDTO(); 
		res.setSpanID("span"); 
		res.setTraceID("trace"); 
		
		Mockito.doReturn(res).when(statusCheckClient)
			.callSearchEventByWorkflowInstanceId(anyString()); 
		
		mvc.perform(get("http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/status/")
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpectAll(status().is2xxSuccessful())
				.andExpect(status().isOk()); 
		
	}

}
