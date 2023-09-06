package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EdsStrategyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class EdsStrategyCFG {

    @Autowired
    private IConfigClient client;

    private EdsStrategyEnum strategy;

    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        this.strategy = client.getStrategyEds();
    }

    public boolean isNoEds() {
        return strategy == null || strategy == EdsStrategyEnum.NO_EDS;
    }

    public boolean isNoFhirEds() {
        return strategy == EdsStrategyEnum.NO_FHIR_EDS;
    }
}
