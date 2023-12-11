package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Config.PROPS_NAME_AUDIT_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Config.PROPS_NAME_CONTROL_LOG_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Config.PROPS_NAME_ISSUER_CF;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.client.routes.base.ClientRoutes.Config.PROPS_NAME_SUBJECT;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ConfigItemDTO.*;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ConfigItemTypeEnum.DISPATCHER;
import static it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ConfigItemTypeEnum.GENERIC;

@Slf4j
@Service
public class ConfigSRV implements IConfigSRV {

    private static final long DELTA_MS = 300_000L;

    @Autowired
    private IConfigClient client;

	private final Map<String, Pair<Long, String>> props;

	public ConfigSRV() {
		this.props = new HashMap<>();
	}

    
    @PostConstruct
    public void postConstruct() {
        for(ConfigItemTypeEnum en : ConfigItemTypeEnum.values()) {
            log.info("[GTW-CFG] Retrieving {} properties ...", en.name());
            ConfigItemDTO items = client.getConfigurationItems(en);
            List<ConfigDataItemDTO> opts = items.getConfigurationItems();
            for(ConfigDataItemDTO opt : opts) {
                opt.getItems().forEach((key, value) -> {
                    log.info("[GTW-CFG] Property {} is set as {}", key, value);
                    props.put(key, Pair.of(new Date().getTime(), value));
                });
            }
        }
    }

    @Override
    public Boolean isAuditEnable() {
        long lastUpdate = props.get(PROPS_NAME_AUDIT_ENABLED).getKey();
        if (new Date().getTime() - lastUpdate >= DELTA_MS) {
            synchronized(PROPS_NAME_CONTROL_LOG_ENABLED) {
                if (new Date().getTime() - lastUpdate >= DELTA_MS) {
                    refresh(DISPATCHER, PROPS_NAME_AUDIT_ENABLED);
                }
            }
        }
        return Boolean.parseBoolean(
            props.get(PROPS_NAME_AUDIT_ENABLED).getValue()
        );
    }

    @Override
    public Boolean isControlLogPersistenceEnable() {
        long lastUpdate = props.get(PROPS_NAME_CONTROL_LOG_ENABLED).getKey();
        if (new Date().getTime() - lastUpdate >= DELTA_MS) {
            synchronized(PROPS_NAME_CONTROL_LOG_ENABLED) {
                if (new Date().getTime() - lastUpdate >= DELTA_MS) {
                    refresh(GENERIC, PROPS_NAME_CONTROL_LOG_ENABLED);
                }
            }
        }
        return Boolean.parseBoolean(
            props.get(PROPS_NAME_CONTROL_LOG_ENABLED).getValue()
        );
    }
  
    @Override
	public Boolean isSubjectPersistenceEnabled() {
		long lastUpdate = props.get(PROPS_NAME_SUBJECT).getKey();
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized (PROPS_NAME_SUBJECT) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refresh(GENERIC, PROPS_NAME_SUBJECT);
				}
			}
		}
		return Boolean.parseBoolean(
			props.get(PROPS_NAME_SUBJECT).getValue()
		);
	}
    
    @Override
	public Boolean isCfOnIssuerNotAllowed() {
		long lastUpdate = props.get(PROPS_NAME_ISSUER_CF).getKey();
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized(PROPS_NAME_ISSUER_CF) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refresh(GENERIC, PROPS_NAME_ISSUER_CF);
				}
			}
		}
		return Boolean.parseBoolean(
			props.get(PROPS_NAME_ISSUER_CF).getValue()
		);
	}

    private void refresh(ConfigItemTypeEnum type, String name) {
        String previous = props.getOrDefault(name, Pair.of(0L, null)).getValue();
        String prop = client.getProps(type, name, previous);
        props.put(name, Pair.of(new Date().getTime(), prop));
    }
}
