package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Config.PROPS_NAME_AUDIT_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ConfigItemTypeEnum.GENERIC;

@Slf4j
@Service
public class ConfigSRV implements IConfigSRV {

    private static final long DELTA_MS = 300_000L;

    @Autowired
    private IConfigClient client;

    private Boolean audit;
    private long lastUpdate;


    @EventListener(ApplicationStartedEvent.class)
    public void initialize() {
        refreshAuditEnable();
    }

    private void refreshAuditEnable() {
        // Keep track before updating
        boolean previous = audit;
        // Execute request and update last-updated
        audit = client.isAuditEnable(GENERIC, PROPS_NAME_AUDIT_ENABLED);
        lastUpdate = new Date().getTime();
        // Omit initialization but notify for subsequent changes
        if(audit != null && previous != audit) {
            log.info(
                "[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})",
                PROPS_NAME_AUDIT_ENABLED,
                audit,
                previous
            );
        }
    }

    @Override
    public Boolean isAuditEnable() {
        if (new Date().getTime() - lastUpdate >= DELTA_MS) {
            synchronized(ConfigSRV.class) {
                refreshAuditEnable();
            }
        }
        return audit;
    }

}
