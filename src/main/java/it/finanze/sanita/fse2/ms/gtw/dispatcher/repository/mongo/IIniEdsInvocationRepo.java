package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.IniEdsInvocationETY;

public interface IIniEdsInvocationRepo extends Serializable {

	IniEdsInvocationETY insert(IniEdsInvocationETY ety);
}
