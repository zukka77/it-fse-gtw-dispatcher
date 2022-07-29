/**
 * 
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import java.io.Serializable;

/**
 * @author AndreaPerquoti
 *
 */
public interface IEdsClient extends Serializable {
	
	Object delete(final String oid);

}
