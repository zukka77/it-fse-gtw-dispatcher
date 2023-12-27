package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.impl.EngineRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl.EngineSRV;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
public class EngineSRVTest {

    @Autowired
    EngineSRV engineSRV;

    @MockBean
    EngineRepo engineRepo;

    @Test
    void testGetStructureObjectID_Exception(){
        when(engineRepo.getLatestEngine()).thenReturn(null);

        assertThrows(
                BusinessException.class,
                () -> engineSRV.getStructureObjectID("test")
        );
    }

}
