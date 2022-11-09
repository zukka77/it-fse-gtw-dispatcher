/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.ValidatedDocumentsETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IValidatedDocumentsRepo;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@AutoConfigureMockMvc
@ActiveProfiles(Constants.Profile.TEST)
@Slf4j
class DateUpdateTest extends AbstractTest {

	@Autowired
	private ServletWebServerApplicationContext webServerAppCtxt;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private IValidatedDocumentsRepo repo;

	@Autowired
	private MongoTemplate mongoTemplate;

	@SpyBean
	private RestTemplate restTemplate;

	@BeforeEach
	void init() {
		mongoTemplate.dropCollection(ValidatedDocumentsETY.class);
	}

	@Test
	@DisplayName("Test connection endpoint")
	void testConnectionEp() throws Exception {

		mvc.perform(get("http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/status")
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpectAll(status().is2xxSuccessful())
				.andExpect(status().isOk());

	}

	@Test
	@DisplayName("Update document's data")
	void updateDataRepo() {

		Date date = new Date();

		ValidatedDocumentsETY ety = new ValidatedDocumentsETY();
		ety.setWorkflowInstanceId("test");
		ety.setInsertionDate(date);

		mongoTemplate.insert(ety);

		assertEquals(repo.updateInsertionDate("test", 7), ety.getId());

		Query query = new Query();
		query.addCriteria(Criteria.where("w_id").is("test"));

		log.info(mongoTemplate.find(query, ValidatedDocumentsETY.class).toString());
	}

}
