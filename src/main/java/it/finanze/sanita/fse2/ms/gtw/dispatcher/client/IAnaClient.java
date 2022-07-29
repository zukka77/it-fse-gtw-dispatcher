package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import java.io.Serializable;


public interface IAnaClient extends Serializable{
	
	Boolean callAnaClient(String codFiscale);
}
