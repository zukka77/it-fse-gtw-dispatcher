package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IEngineRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IEngineSRV;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EngineSRV implements IEngineSRV {

    @Autowired
    private IEngineRepo engines;

    @Override
    public Pair<String, String> getStructureObjectID(String templateId) {
        Pair<String, String> p;

        try{
            EngineETY latest = engines.getLatestEngine();

            if(latest == null) throw new BusinessException("Nessun engine disponibile");

            Optional<EngineMap> map = latest.getRoots().stream().filter(r -> r.getRoot().contains(templateId)).findFirst();

            if(!map.isPresent()) {
                throw new BusinessException(
                    String.format("Nessuna mappa con id %s è stata trovata nell'engine %s", templateId, latest.getId())
                );
            }

            p = Pair.of(latest.getId(), map.get().getOid());

        } catch(Exception ex){
            throw new BusinessException("Impossibile recuperare la structure-map nell'engine associato", ex);
        }

        return p;
    }
}
