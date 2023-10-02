package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IEngineRepo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
public class EngineRepoTest {

    @Autowired
	private MongoTemplate mongoTemplate;

    @Autowired
    private IEngineRepo repository;

    @BeforeEach
	void init() {
		mongoTemplate.dropCollection(EngineETY.class);
	}

    @Test
    void getLatestEngineTest() {
        // Data preparation
        EngineETY engine = new EngineETY();
        engine.setId("id_test");
        engine.setLastSync(new Date());
        engine.setAvailable(true);
        // Insert engine on DB
        mongoTemplate.insert(engine);
        // Perform getLatestEngine()
        EngineETY response = repository.getLatestEngine();
        // Assertions
        assertEquals(engine.getId(), response.getId());
        assertEquals(engine.getLastSync(), response.getLastSync());
        assertTrue(response.isAvailable());
    }
    
}
