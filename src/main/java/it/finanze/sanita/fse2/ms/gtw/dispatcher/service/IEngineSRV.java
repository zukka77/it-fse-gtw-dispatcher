package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

import org.apache.commons.lang3.tuple.Pair;

public interface IEngineSRV {
    Pair<String, String> getStructureObjectID(String templateIdRoot);
}
