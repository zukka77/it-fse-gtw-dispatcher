package it.finanze.sanita.fse2.ms.gtw.dispatcher.service;

public interface IConfigSRV {

	String getEdsStrategy();

	boolean isNoEds();
	
	boolean isNoFhirEds();
}
